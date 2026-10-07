package com.bivora.karaliste.guncelleme

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.bivora.karaliste.AnaAktivite
import com.bivora.karaliste.R

object Bildirimler {

    private const val KANAL = "guncelleme"
    private const val BILDIRIM_GUNCELLEME_VAR = 1
    private const val BILDIRIM_GUNCELLENDI = 2
    private const val TERCIHLER = "guncelleme"
    private const val BILDIRILEN_SURUM = "bildirilenSurum"

    fun kanalOlustur(context: Context) {
        val kanal = NotificationChannel(
            KANAL,
            context.getString(R.string.bildirim_kanal_guncelleme),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(kanal)
    }

    // Aynı sürüm için yalnızca bir kez bildirilir
    fun guncellemeVar(context: Context, bilgi: SurumBilgisi) {
        val tercihler = context.getSharedPreferences(TERCIHLER, Context.MODE_PRIVATE)
        if (tercihler.getInt(BILDIRILEN_SURUM, 0) >= bilgi.surumKodu) return

        if (goster(
                context,
                BILDIRIM_GUNCELLEME_VAR,
                context.getString(R.string.bildirim_guncelleme_baslik, bilgi.surumAdi),
                bilgi.notlar.firstOrNull() ?: context.getString(R.string.bildirim_guncelleme_metin)
            )
        ) {
            tercihler.edit { putInt(BILDIRILEN_SURUM, bilgi.surumKodu) }
        }
    }

    fun guncellendi(context: Context, surumAdi: String) {
        NotificationManagerCompat.from(context).cancel(BILDIRIM_GUNCELLEME_VAR)
        goster(
            context,
            BILDIRIM_GUNCELLENDI,
            context.getString(R.string.bildirim_guncellendi_baslik, surumAdi),
            context.getString(R.string.bildirim_guncellendi_metin)
        )
    }

    // İzin yoksa veya bildirimler kapalıysa false döner
    @SuppressLint("MissingPermission")
    private fun goster(context: Context, id: Int, baslik: String, metin: String): Boolean {
        val yonetici = NotificationManagerCompat.from(context)
        if (!yonetici.areNotificationsEnabled()) return false
        kanalOlustur(context)

        val ac = PendingIntent.getActivity(
            context,
            id,
            Intent(context, AnaAktivite::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bildirim = NotificationCompat.Builder(context, KANAL)
            .setSmallIcon(R.drawable.ic_bildirim)
            .setColor(context.getColor(R.color.bivora_mor))
            .setContentTitle(baslik)
            .setContentText(metin)
            .setContentIntent(ac)
            .setAutoCancel(true)
            .build()

        yonetici.notify(id, bildirim)
        return true
    }
}
