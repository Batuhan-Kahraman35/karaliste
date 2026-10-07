package com.bivora.karaliste.ui

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bivora.karaliste.R
import com.bivora.karaliste.ag.ApiHatasi
import com.bivora.karaliste.ag.HesapServisi
import com.bivora.karaliste.ag.OturumBilgisi
import com.bivora.karaliste.ag.OturumDeposu
import com.bivora.karaliste.ag.Senkronizasyon
import com.bivora.karaliste.guncelleme.GuncellemeDurumu
import com.bivora.karaliste.guncelleme.GuncellemeYoneticisi
import com.bivora.karaliste.veri.EngellenenArama
import com.bivora.karaliste.veri.EslesmeTipi
import com.bivora.karaliste.veri.Karaliste
import com.bivora.karaliste.veri.NumaraYardimci
import com.bivora.karaliste.veri.VeriTabani
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class AnaViewModel(uygulama: Application) : AndroidViewModel(uygulama) {

    private val dao = VeriTabani.al(uygulama).karalisteDao()
    private val engellenenDao = VeriTabani.al(uygulama).engellenenAramaDao()
    private val oturumDeposu = OturumDeposu.al(uygulama)
    private val senkron = Senkronizasyon.al(uygulama)
    private val hesap = HesapServisi(uygulama)
    private val guncelleme = GuncellemeYoneticisi.al(uygulama)

    val guncellemeDurumu: StateFlow<GuncellemeDurumu> = guncelleme.durum
    val zorunluGuncelleme: StateFlow<Boolean> = guncelleme.zorunlu

    fun guncellemeKontrolEt() = guncelleme.arkaplandaKontrolEt()
    fun guncellemeyiBaslat() = guncelleme.baslat()

    val liste: StateFlow<List<Karaliste>> = dao.tumu()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val eslesmeTipleri: StateFlow<List<EslesmeTipi>> = dao.eslesmeTipleri()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val engellenenler: StateFlow<List<EngellenenArama>> = engellenenDao.tumu()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val oturum: StateFlow<OturumBilgisi?> = oturumDeposu.durum
    val senkronCalisiyor: StateFlow<Boolean> = senkron.calisiyor

    private val _islemSuruyor = MutableStateFlow(false)
    val islemSuruyor: StateFlow<Boolean> = _islemSuruyor.asStateFlow()

    // Ekran değişse de mesaj kaybolmasın diye kanal kullanılır
    private val _mesaj = Channel<String>(Channel.BUFFERED)
    val mesaj: Flow<String> = _mesaj.receiveAsFlow()

    // ---- Karaliste ----

    fun ekle(numara: String, aciklama: String, eslesmeTipiId: Int) {
        viewModelScope.launch {
            val desen = eslesmeTipiId == EslesmeTipi.DESEN
            val sade = if (desen) NumaraYardimci.desenSadelestir(numara) else NumaraYardimci.sadelestir(numara)

            val hata = when {
                desen && NumaraYardimci.JOKER !in sade -> R.string.hata_desen_joker
                desen && sade.length != NumaraYardimci.DESEN_HANE -> R.string.hata_desen_uzunluk
                !desen && sade.length < NumaraYardimci.MIN_HANE -> R.string.hata_gecersiz_numara
                dao.ayniVarMi(sade, eslesmeTipiId) -> R.string.hata_numara_var
                else -> null
            }
            if (hata != null) {
                mesajGonder(hata)
                return@launch
            }

            dao.ekle(
                Karaliste(
                    guid = UUID.randomUUID().toString(),
                    numara = sade,
                    gorunenNumara = numara.trim(),
                    eslesmeTipiId = eslesmeTipiId,
                    aciklama = aciklama.trim().ifBlank { null }
                )
            )
            mesajGonder(R.string.basarili_eklendi)
            senkronIste()
        }
    }

    fun durumDegistir(kayit: Karaliste, durum: Boolean) {
        viewModelScope.launch {
            dao.durumGuncelle(kayit.id, durum, System.currentTimeMillis())
            senkronIste()
        }
    }

    fun sil(kayit: Karaliste) {
        viewModelScope.launch {
            // Üyelikte silme sunucuya iletilene kadar işaretli tutulur
            if (oturum.value != null) dao.yumusakSil(kayit.id, System.currentTimeMillis())
            else dao.sil(kayit.id)
            mesajGonder(R.string.basarili_silindi)
            senkronIste()
        }
    }

    // ---- Engellenen aramalar ----

    fun engelleneniSil(kayit: EngellenenArama) {
        viewModelScope.launch { engellenenDao.sil(kayit.id) }
    }

    fun engellenenleriTemizle() {
        viewModelScope.launch {
            engellenenDao.tumunuSil()
            mesajGonder(R.string.gecmis_temizlendi)
        }
    }

    // Otomatik senkronda yalnızca oturum düşmesi bildirilir; elle senkronda her sonuç gösterilir
    fun senkronIste(elle: Boolean = false) {
        if (oturum.value == null) return
        viewModelScope.launch {
            val hata = senkron.calistir()
            when {
                oturum.value == null -> _mesaj.send(hata ?: metin(R.string.hata_oturum_sona_erdi))
                hata != null && elle -> _mesaj.send(hata)
                elle -> mesajGonder(R.string.senkron_tamam)
            }
        }
    }

    // ---- Hesap ----

    fun giris(eposta: String, sifre: String, sonuc: (String?) -> Unit) =
        hesapIslemi(sonuc) {
            hesap.giris(eposta, sifre)
            mesajGonder(R.string.giris_yapildi)
            senkronIste()
            null
        }

    fun kayit(adSoyad: String, eposta: String, sifre: String, sonuc: (String?) -> Unit) =
        hesapIslemi(sonuc) {
            hesap.kayit(adSoyad.ifBlank { null }, eposta, sifre)
            mesajGonder(R.string.kayit_yapildi)
            senkronIste()
            null
        }

    fun cikis() {
        viewModelScope.launch {
            // Bekleyen değişiklikler önce gönderilir
            senkron.calistir()
            hesap.cikis()
            mesajGonder(R.string.cikis_yapildi)
        }
    }

    fun hesapSil(sifre: String, sonuc: (String?) -> Unit) =
        hesapIslemi(sonuc) {
            hesap.hesapSil(sifre)
            mesajGonder(R.string.hesap_silindi)
            null
        }

    // sonuc: (hata, bilgi)
    fun sifreSifirlaIste(eposta: String, sonuc: (String?, String?) -> Unit) {
        var bilgi: String? = null
        hesapIslemi({ hata -> sonuc(hata, bilgi) }) {
            bilgi = hesap.sifreSifirlaIste(eposta)
            null
        }
    }

    fun sifreSifirla(eposta: String, kod: String, yeniSifre: String, sonuc: (String?) -> Unit) =
        hesapIslemi(sonuc) {
            hesap.sifreSifirla(eposta, kod, yeniSifre)?.let { _mesaj.send(it) }
            null
        }

    // Bloğun döndürdüğü veya fırlattığı hata mesajı sonuc'a iletilir
    private fun hesapIslemi(sonuc: (String?) -> Unit, blok: suspend () -> String?) {
        viewModelScope.launch {
            _islemSuruyor.value = true
            val hata = try {
                blok()
            } catch (e: ApiHatasi) {
                e.message ?: metin(R.string.hata_sunucu)
            } catch (e: IOException) {
                metin(R.string.hata_baglanti)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                metin(R.string.hata_sunucu)
            }
            _islemSuruyor.value = false
            sonuc(hata)
        }
    }

    private fun metin(@StringRes id: Int): String = getApplication<Application>().getString(id)

    private suspend fun mesajGonder(@StringRes id: Int) {
        _mesaj.send(metin(id))
    }
}
