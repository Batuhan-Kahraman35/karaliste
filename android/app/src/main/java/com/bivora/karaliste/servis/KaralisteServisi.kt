package com.bivora.karaliste.servis

import android.telecom.Call
import android.telecom.CallScreeningService
import com.bivora.karaliste.veri.EngellenenArama
import com.bivora.karaliste.veri.NumaraYardimci
import com.bivora.karaliste.veri.VeriTabani
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class KaralisteServisi : CallScreeningService() {

    private val kapsam = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(detay: Call.Details) {
        if (detay.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(detay, CallResponse.Builder().build())
            return
        }

        val numara = detay.handle?.schemeSpecificPart

        kapsam.launch {
            val vt = VeriTabani.al(applicationContext)
            val dao = vt.karalisteDao()
            val sade = numara?.takeIf { it.isNotBlank() }?.let(NumaraYardimci::sadelestir)
            // Güvenilen bir kurala uyan arama, karalistede eşleşse bile geçer ve kaydedilmez
            val kural = when {
                sade == null -> null
                dao.eslesenKural(sade, izinli = true) != null -> null
                else -> dao.eslesenKural(sade, izinli = false)
            }
            val engelle = kural != null

            // Önce yanıt verilir; kayıt aramanın engellenmesini geciktirmesin
            respondToCall(
                detay,
                CallResponse.Builder()
                    .setDisallowCall(engelle)
                    .setRejectCall(engelle)
                    .setSkipCallLog(false)
                    .setSkipNotification(engelle)
                    .build()
            )

            if (kural != null && numara != null) {
                val gecmis = vt.engellenenAramaDao()
                gecmis.ekle(
                    EngellenenArama(
                        numara = numara,
                        kuralNumara = kural.gorunenNumara,
                        eslesmeTipiId = kural.eslesmeTipiId
                    )
                )
                gecmis.eskileriTemizle(GECMIS_SINIRI)
            }
        }
    }

    override fun onDestroy() {
        kapsam.cancel()
        super.onDestroy()
    }

    private companion object {
        const val GECMIS_SINIRI = 1000
    }
}
