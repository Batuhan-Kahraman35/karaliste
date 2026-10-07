package com.bivora.karaliste.ag

import android.content.Context
import androidx.room.withTransaction
import com.bivora.karaliste.R
import com.bivora.karaliste.veri.EslesmeTipi
import com.bivora.karaliste.veri.Karaliste
import com.bivora.karaliste.veri.VeriTabani
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/*
 * 1) Senkronlanmamış yerel değişiklikler gönderilir
 * 2) Sunucu, sonSenkron'dan sonra değişen kayıtları döner
 * 3) Gelen kayıtlar yerele yazılır; yerelde gönderilmemiş yeni değişiklik varsa o kayıt atlanır
 */
class Senkronizasyon private constructor(private val context: Context) {

    private val vt = VeriTabani.al(context)
    private val dao = vt.karalisteDao()
    private val oturum = OturumDeposu.al(context)
    private val kilit = Mutex()

    private val _calisiyor = MutableStateFlow(false)
    val calisiyor: StateFlow<Boolean> = _calisiyor.asStateFlow()

    // Başarılıysa veya oturum yoksa null, aksi halde hata mesajı döner
    suspend fun calistir(): String? = kilit.withLock {
        val token = oturum.token ?: return null
        _calisiyor.value = true
        try {
            runCatching { eslesmeTipleriniGuncelle() }

            val gidenler = dao.senkronlanmamislar()
            val govde = JSONObject()
                .put("sonSenkron", oturum.sonSenkron ?: JSONObject.NULL)
                // Sunucu güvenilen kayıtları yalnızca bunu bildiren istemcilere gönderir
                .put("izinliDestegi", true)
                .put("degisiklikler", JSONArray().apply { gidenler.forEach { put(it.json()) } })

            val veri = ApiIstemci.istek("senkron", govde, token).getJSONObject("veri")

            vt.withTransaction {
                gidenler.forEach { dao.senkronlandiIsaretle(it.guid, it.guncellemeTarihi ?: 0) }

                // Sunucunun reddettiği kayıtlar tekrar tekrar gönderilmesin
                val hatalar = veri.optJSONArray("hatalar") ?: JSONArray()
                for (i in 0 until hatalar.length()) {
                    hatalar.optJSONObject(i)?.metinVeyaNull("guid")?.let { dao.senkronlandiIsaretleGuid(it) }
                }

                val kayitlar = veri.getJSONArray("kayitlar")
                for (i in 0 until kayitlar.length()) {
                    gelenKaydiIsle(kayitlar.getJSONObject(i))
                }

                dao.senkronlanmisSilinmisleriTemizle()
            }

            oturum.sonSenkron = veri.getString("sunucuZamani")
            null
        } catch (e: ApiHatasi) {
            if (e.kod == 401) {
                oturum.temizle()
                dao.silinmisleriTemizle()
                context.getString(R.string.hata_oturum_sona_erdi)
            } else {
                e.message ?: context.getString(R.string.hata_sunucu)
            }
        } catch (e: IOException) {
            context.getString(R.string.hata_baglanti)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Beklenmeyen yanıt biçimi (JSONException vb.)
            context.getString(R.string.hata_sunucu)
        } finally {
            _calisiyor.value = false
        }
    }

    private suspend fun gelenKaydiIsle(j: JSONObject) {
        val guid = j.getString("guid")
        val yerel = dao.guidIle(guid)

        if (yerel != null && !yerel.senkronlandi) return

        if (j.getBoolean("silindi")) {
            if (yerel != null) dao.guidSil(guid)
            return
        }

        val kayit = Karaliste(
            id = yerel?.id ?: 0,
            guid = guid,
            numara = j.getString("numara"),
            gorunenNumara = j.getString("gorunenNumara"),
            eslesmeTipiId = j.getInt("eslesmeTipiId"),
            aciklama = j.metinVeyaNull("aciklama"),
            izinli = j.optBoolean("izinli", false),
            durum = j.getBoolean("durum"),
            silindi = false,
            senkronlandi = true,
            olusturmaTarihi = yerel?.olusturmaTarihi ?: System.currentTimeMillis(),
            guncellemeTarihi = yerel?.guncellemeTarihi ?: System.currentTimeMillis()
        )
        if (yerel == null) dao.ekle(kayit) else dao.guncelle(kayit)
    }

    private suspend fun eslesmeTipleriniGuncelle() {
        val tipler = ApiIstemci.istek("eslesme-tipleri").getJSONObject("veri").getJSONArray("tipler")
        dao.eslesmeTipleriniKaydet(
            (0 until tipler.length()).map { i ->
                val t = tipler.getJSONObject(i)
                EslesmeTipi(
                    id = t.getInt("id"),
                    ad = t.getString("ad"),
                    aciklama = t.getString("aciklama"),
                    ornek = t.getString("ornek"),
                    sira = t.getInt("sira")
                )
            }
        )
    }

    companion object {
        @Volatile
        private var ornek: Senkronizasyon? = null

        fun al(context: Context): Senkronizasyon =
            ornek ?: synchronized(this) {
                ornek ?: Senkronizasyon(context.applicationContext).also { ornek = it }
            }
    }
}
