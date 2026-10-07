package com.bivora.karaliste

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.bivora.karaliste.guncelleme.Bildirimler
import com.bivora.karaliste.guncelleme.GuncellemeKontrolIsi
import com.bivora.karaliste.ui.AnaViewModel
import com.bivora.karaliste.ui.Uygulama
import com.bivora.karaliste.ui.tema.KaralisteTema

class AnaAktivite : ComponentActivity() {

    private val vm: AnaViewModel by viewModels()
    private val rolYoneticisi by lazy { getSystemService(RoleManager::class.java) }
    private var rolVar by mutableStateOf(false)

    private val rolIstegi = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { rolVar = rolKontrol() }

    // "Bilinmeyen uygulamaları yükle" ayarından dönünce izin verildiyse güncelleme devam eder
    private val yuklemeIzniIstegi = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (packageManager.canRequestPackageInstalls()) vm.guncellemeyiBaslat()
    }

    private val bildirimIzniIstegi = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        rolVar = rolKontrol()

        Bildirimler.kanalOlustur(this)
        GuncellemeKontrolIsi.planla(this)
        bildirimIzniIste()

        setContent {
            KaralisteTema {
                Uygulama(vm = vm, rolVar = rolVar, rolIste = ::rolIste, guncelle = ::guncelle)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rolVar = rolKontrol()
        // Uygulamaya her dönüşte diğer cihazlardaki değişiklikler ve yeni sürüm kontrol edilir
        vm.senkronIste()
        vm.guncellemeKontrolEt()
    }

    private fun rolKontrol(): Boolean =
        rolYoneticisi.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)

    private fun rolIste() {
        if (rolYoneticisi.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            rolIstegi.launch(rolYoneticisi.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
        }
    }

    private fun guncelle() {
        if (packageManager.canRequestPackageInstalls()) {
            vm.guncellemeyiBaslat()
        } else {
            yuklemeIzniIstegi.launch(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName"))
            )
        }
    }

    // Android 13+: güncelleme bildirimleri için bir kez sorulur
    private fun bildirimIzniIste() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return

        val tercihler = getSharedPreferences("izinler", MODE_PRIVATE)
        if (tercihler.getBoolean("bildirimSoruldu", false)) return
        tercihler.edit { putBoolean("bildirimSoruldu", true) }
        bildirimIzniIstegi.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
