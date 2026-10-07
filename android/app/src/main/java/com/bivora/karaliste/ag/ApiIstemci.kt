package com.bivora.karaliste.ag

import com.bivora.karaliste.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Sunucunun döndürdüğü hata; mesaj sunucudan Türkçe gelir
class ApiHatasi(val kod: Int, mesaj: String?) : Exception(mesaj)

object ApiIstemci {

    private const val BAGLANTI_SURESI = 15_000
    private const val OKUMA_SURESI = 30_000

    // Ağ hatasında IOException fırlatır
    suspend fun istek(
        yol: String,
        govde: JSONObject? = null,
        token: String? = null,
        yontem: String = if (govde != null) "POST" else "GET"
    ): JSONObject = withContext(Dispatchers.IO) {
        val baglanti = URL(BuildConfig.API_ADRES + yol).openConnection() as HttpURLConnection
        try {
            baglanti.requestMethod = yontem
            baglanti.connectTimeout = BAGLANTI_SURESI
            baglanti.readTimeout = OKUMA_SURESI
            baglanti.setRequestProperty("Accept", "application/json")
            token?.let { baglanti.setRequestProperty("Authorization", "Bearer $it") }

            if (govde != null) {
                baglanti.doOutput = true
                baglanti.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                baglanti.outputStream.use { it.write(govde.toString().toByteArray(Charsets.UTF_8)) }
            }

            val kod = baglanti.responseCode
            val akis = if (kod in 200..299) baglanti.inputStream else baglanti.errorStream
            val metin = akis?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(metin) }.getOrNull()

            if (kod !in 200..299 || json?.optBoolean("basarili") != true) {
                throw ApiHatasi(kod, json?.metinVeyaNull("mesaj"))
            }
            json
        } finally {
            baglanti.disconnect()
        }
    }
}

// org.json null değeri "null" metni olarak döndürür; bu yardımcı gerçek null verir
fun JSONObject.metinVeyaNull(ad: String): String? =
    if (isNull(ad)) null else optString(ad).ifBlank { null }
