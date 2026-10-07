package com.bivora.karaliste.guncelleme

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import com.bivora.karaliste.BuildConfig
import com.bivora.karaliste.R
import com.bivora.karaliste.ag.ApiIstemci
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class SurumBilgisi(
    val surumKodu: Int,
    val surumAdi: String,
    val minSurumKodu: Int,
    val notlar: List<String>,
    val indirmeAdresi: String,
    val boyut: Long,
    val sha256: String
)

sealed interface GuncellemeDurumu {
    data object Yok : GuncellemeDurumu
    data class Var(val bilgi: SurumBilgisi) : GuncellemeDurumu
    data class Indiriliyor(val bilgi: SurumBilgisi, val yuzde: Int) : GuncellemeDurumu
    data class Kuruluyor(val bilgi: SurumBilgisi) : GuncellemeDurumu
    data class Hata(val bilgi: SurumBilgisi, val mesaj: String) : GuncellemeDurumu
}

/*
 * 1) /api/surum ile yeni sürüm sorulur
 * 2) APK indirilir, SHA-256 doğrulanır
 * 3) PackageInstaller oturumuyla kurulur. Android 12+ ve uygulama kendi yükleyicisiyse onay istenmeyebilir;
 *    aksi halde sistem onay ekranı KurulumAlici üzerinden açılır.
 */
class GuncellemeYoneticisi private constructor(private val context: Context) {

    private val kapsam = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var islem: Job? = null

    private val _durum = MutableStateFlow<GuncellemeDurumu>(GuncellemeDurumu.Yok)
    val durum: StateFlow<GuncellemeDurumu> = _durum.asStateFlow()

    // Bu sürüm artık desteklenmiyorsa true
    private val _zorunlu = MutableStateFlow(false)
    val zorunlu: StateFlow<Boolean> = _zorunlu.asStateFlow()

    private val klasor: File get() = File(context.cacheDir, "guncelleme")

    // Yeni sürüm varsa bilgisini döner; ağ hatasında IOException fırlatır
    suspend fun kontrolEt(): SurumBilgisi? {
        val veri = ApiIstemci.istek("surum").getJSONObject("veri")
        if (veri.isNull("surumKodu")) {
            _zorunlu.value = false
            if (_durum.value is GuncellemeDurumu.Var) _durum.value = GuncellemeDurumu.Yok
            return null
        }

        val notlar = veri.optJSONArray("notlar")
        val bilgi = SurumBilgisi(
            surumKodu = veri.getInt("surumKodu"),
            surumAdi = veri.optString("surumAdi"),
            minSurumKodu = veri.optInt("minSurumKodu", 1),
            notlar = (0 until (notlar?.length() ?: 0)).map { notlar!!.getString(it) },
            indirmeAdresi = veri.getString("indirmeAdresi"),
            boyut = veri.optLong("boyut"),
            sha256 = veri.getString("sha256").lowercase()
        )

        _zorunlu.value = BuildConfig.VERSION_CODE < bilgi.minSurumKodu
        val yeni = bilgi.surumKodu > BuildConfig.VERSION_CODE

        // İndirme/kurulum sürerken durum ezilmez
        val mevcut = _durum.value
        if (mevcut is GuncellemeDurumu.Yok || mevcut is GuncellemeDurumu.Var || mevcut is GuncellemeDurumu.Hata) {
            _durum.value = if (yeni) GuncellemeDurumu.Var(bilgi) else GuncellemeDurumu.Yok
        }
        return if (yeni) bilgi else null
    }

    fun arkaplandaKontrolEt() {
        kapsam.launch { runCatching { kontrolEt() } }
    }

    fun baslat() {
        val bilgi = when (val d = _durum.value) {
            is GuncellemeDurumu.Var -> d.bilgi
            is GuncellemeDurumu.Hata -> d.bilgi
            else -> return
        }
        if (islem?.isActive == true) return

        islem = kapsam.launch {
            try {
                val dosya = indir(bilgi)
                _durum.value = GuncellemeDurumu.Kuruluyor(bilgi)
                kur(dosya)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                _durum.value = GuncellemeDurumu.Hata(bilgi, e.message ?: context.getString(R.string.hata_guncelleme_indirme))
            } catch (e: Exception) {
                _durum.value = GuncellemeDurumu.Hata(bilgi, context.getString(R.string.hata_guncelleme_kurulum))
            }
        }
    }

    // KurulumAlici'dan gelir
    fun kurulumBitti(basarili: Boolean, iptal: Boolean, mesaj: String?) {
        val bilgi = (_durum.value as? GuncellemeDurumu.Kuruluyor)?.bilgi ?: return
        _durum.value = when {
            basarili -> GuncellemeDurumu.Yok
            iptal -> GuncellemeDurumu.Var(bilgi)
            else -> GuncellemeDurumu.Hata(bilgi, mesaj ?: context.getString(R.string.hata_guncelleme_kurulum))
        }
    }

    fun indirilenleriTemizle() {
        klasor.deleteRecursively()
    }

    private suspend fun indir(bilgi: SurumBilgisi): File = withContext(Dispatchers.IO) {
        klasor.mkdirs()
        val dosya = File(klasor, "karaliste-${bilgi.surumKodu}.apk")
        if (dosya.exists() && sha256(dosya) == bilgi.sha256) return@withContext dosya

        _durum.value = GuncellemeDurumu.Indiriliyor(bilgi, 0)
        val baglanti = URL(bilgi.indirmeAdresi).openConnection() as HttpURLConnection
        try {
            baglanti.connectTimeout = 15_000
            baglanti.readTimeout = 30_000
            if (baglanti.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException(context.getString(R.string.hata_guncelleme_indirme))
            }
            val toplam = baglanti.contentLengthLong.takeIf { it > 0 } ?: bilgi.boyut
            var okunan = 0L
            var sonYuzde = -1

            baglanti.inputStream.use { giris ->
                dosya.outputStream().use { cikis ->
                    val tampon = ByteArray(64 * 1024)
                    while (true) {
                        val n = giris.read(tampon)
                        if (n < 0) break
                        cikis.write(tampon, 0, n)
                        okunan += n
                        val yuzde = if (toplam > 0) ((okunan * 100) / toplam).toInt().coerceIn(0, 100) else 0
                        if (yuzde != sonYuzde) {
                            sonYuzde = yuzde
                            _durum.value = GuncellemeDurumu.Indiriliyor(bilgi, yuzde)
                        }
                    }
                }
            }
        } finally {
            baglanti.disconnect()
        }

        if (sha256(dosya) != bilgi.sha256) {
            dosya.delete()
            throw IOException(context.getString(R.string.hata_guncelleme_dogrulama))
        }
        dosya
    }

    private fun kur(dosya: File) {
        val yukleyici = context.packageManager.packageInstaller
        val parametreler = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            setSize(dosya.length())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            }
        }

        val oturumId = yukleyici.createSession(parametreler)
        yukleyici.openSession(oturumId).use { oturum ->
            dosya.inputStream().use { giris ->
                oturum.openWrite("karaliste.apk", 0, dosya.length()).use { cikis ->
                    giris.copyTo(cikis)
                    oturum.fsync(cikis)
                }
            }
            val geriDonus = PendingIntent.getBroadcast(
                context,
                oturumId,
                Intent(context, KurulumAlici::class.java),
                // Sistem sonuç bilgisini intent'e eklediği için değiştirilebilir olmalı
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            oturum.commit(geriDonus.intentSender)
        }
    }

    private fun sha256(dosya: File): String {
        val ozet = MessageDigest.getInstance("SHA-256")
        dosya.inputStream().use { giris ->
            val tampon = ByteArray(64 * 1024)
            while (true) {
                val n = giris.read(tampon)
                if (n < 0) break
                ozet.update(tampon, 0, n)
            }
        }
        return ozet.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        @Volatile
        private var ornek: GuncellemeYoneticisi? = null

        fun al(context: Context): GuncellemeYoneticisi =
            ornek ?: synchronized(this) {
                ornek ?: GuncellemeYoneticisi(context.applicationContext).also { ornek = it }
            }
    }
}
