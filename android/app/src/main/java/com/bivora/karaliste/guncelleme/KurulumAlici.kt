package com.bivora.karaliste.guncelleme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.core.content.IntentCompat

// PackageInstaller oturumunun sonucunu alır
class KurulumAlici : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val yonetici = GuncellemeYoneticisi.al(context)

        when (val durum = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            // Sessiz kurulum yapılamadı; sistemin onay ekranı açılır
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)?.let {
                    context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            PackageInstaller.STATUS_SUCCESS -> yonetici.kurulumBitti(basarili = true, iptal = false, mesaj = null)
            else -> yonetici.kurulumBitti(
                basarili = false,
                iptal = durum == PackageInstaller.STATUS_FAILURE_ABORTED,
                mesaj = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
            )
        }
    }
}
