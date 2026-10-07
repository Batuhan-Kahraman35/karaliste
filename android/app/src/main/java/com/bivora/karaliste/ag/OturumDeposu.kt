package com.bivora.karaliste.ag

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OturumBilgisi(val eposta: String, val adSoyad: String?)

// Token ve senkron zamanı şifreli olarak saklanır
class OturumDeposu private constructor(context: Context) {

    private val tercihler: SharedPreferences = olustur(context)

    private val _durum = MutableStateFlow(bilgiOku())
    val durum: StateFlow<OturumBilgisi?> = _durum.asStateFlow()

    val token: String?
        get() = tercihler.getString(TOKEN, null)

    // Sunucunun son senkronda verdiği zaman damgası; null ise tam senkron yapılır
    var sonSenkron: String?
        get() = tercihler.getString(SON_SENKRON, null)
        set(deger) = tercihler.edit { putString(SON_SENKRON, deger) }

    fun kaydet(token: String, eposta: String, adSoyad: String?) {
        tercihler.edit {
            putString(TOKEN, token)
            putString(EPOSTA, eposta)
            putString(AD_SOYAD, adSoyad)
            remove(SON_SENKRON)
        }
        _durum.value = bilgiOku()
    }

    fun temizle() {
        tercihler.edit { clear() }
        _durum.value = null
    }

    private fun bilgiOku(): OturumBilgisi? =
        if (token == null) null
        else OturumBilgisi(tercihler.getString(EPOSTA, null).orEmpty(), tercihler.getString(AD_SOYAD, null))

    companion object {
        private const val DOSYA = "oturum"
        private const val TOKEN = "token"
        private const val EPOSTA = "eposta"
        private const val AD_SOYAD = "adSoyad"
        private const val SON_SENKRON = "sonSenkron"

        @Volatile
        private var ornek: OturumDeposu? = null

        fun al(context: Context): OturumDeposu =
            ornek ?: synchronized(this) {
                ornek ?: OturumDeposu(context.applicationContext).also { ornek = it }
            }

        private fun olustur(context: Context): SharedPreferences {
            fun yeni(): SharedPreferences = EncryptedSharedPreferences.create(
                context,
                DOSYA,
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            // Yedekten dönen dosyanın anahtarı cihazda yoksa açılamaz; oturum sıfırlanır
            return try {
                yeni()
            } catch (e: Exception) {
                context.deleteSharedPreferences(DOSYA)
                yeni()
            }
        }
    }
}
