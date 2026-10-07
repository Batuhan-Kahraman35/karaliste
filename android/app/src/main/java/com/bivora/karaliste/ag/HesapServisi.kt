package com.bivora.karaliste.ag

import android.content.Context
import android.os.Build
import com.bivora.karaliste.veri.VeriTabani
import org.json.JSONObject

class HesapServisi(context: Context) {

    private val oturum = OturumDeposu.al(context)
    private val dao = VeriTabani.al(context).karalisteDao()
    private val cihazAdi = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

    suspend fun giris(eposta: String, sifre: String) {
        val yanit = ApiIstemci.istek(
            "giris",
            JSONObject().put("eposta", eposta).put("sifre", sifre).put("cihazAdi", cihazAdi)
        )
        oturumAc(yanit.getJSONObject("veri"))
    }

    suspend fun kayit(adSoyad: String?, eposta: String, sifre: String) {
        val yanit = ApiIstemci.istek(
            "kayit",
            JSONObject()
                .put("adSoyad", adSoyad ?: JSONObject.NULL)
                .put("eposta", eposta)
                .put("sifre", sifre)
                .put("cihazAdi", cihazAdi)
        )
        oturumAc(yanit.getJSONObject("veri"))
    }

    // Sunucu bağlantısı olmasa da yerel oturum kapanır; liste telefonda kalır
    suspend fun cikis() {
        val token = oturum.token
        if (token != null) {
            runCatching { ApiIstemci.istek("cikis", JSONObject(), token) }
        }
        oturum.temizle()
        dao.silinmisleriTemizle()
    }

    // Sunucu silmeyi onaylarsa yerel oturum kapanır; liste telefonda kalır
    suspend fun hesapSil(sifre: String) {
        val token = oturum.token ?: return
        ApiIstemci.istek("hesap-sil", JSONObject().put("sifre", sifre), token)
        oturum.temizle()
        dao.silinmisleriTemizle()
    }

    suspend fun sifreSifirlaIste(eposta: String): String? =
        ApiIstemci.istek("sifre-sifirla-iste", JSONObject().put("eposta", eposta)).metinVeyaNull("mesaj")

    suspend fun sifreSifirla(eposta: String, kod: String, yeniSifre: String): String? =
        ApiIstemci.istek(
            "sifre-sifirla",
            JSONObject().put("eposta", eposta).put("kod", kod).put("yeniSifre", yeniSifre)
        ).metinVeyaNull("mesaj")

    private suspend fun oturumAc(veri: JSONObject) {
        val kullanici = veri.getJSONObject("kullanici")
        // Yereldeki liste hesaba yüklensin
        dao.tumunuSenkronlanmadiYap()
        oturum.kaydet(
            token = veri.getString("token"),
            eposta = kullanici.getString("eposta"),
            adSoyad = kullanici.metinVeyaNull("adSoyad")
        )
    }
}
