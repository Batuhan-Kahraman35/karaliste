package com.bivora.karaliste.guncelleme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bivora.karaliste.BuildConfig

// Uygulama güncellendikten sonra çalışır: indirilen APK silinir, kullanıcıya bildirim gösterilir
class GuncellendiAlici : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        GuncellemeYoneticisi.al(context).indirilenleriTemizle()
        Bildirimler.guncellendi(context, BuildConfig.VERSION_NAME)
    }
}
