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
            val kural = if (numara.isNullOrBlank()) null
            else vt.karalisteDao().eslesenKural(NumaraYardimci.sadelestir(numara))
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
                val dao = vt.engellenenAramaDao()
                dao.ekle(
                    EngellenenArama(
                        numara = numara,
                        kuralNumara = kural.gorunenNumara,
                        eslesmeTipiId = kural.eslesmeTipiId
                    )
                )
                dao.eskileriTemizle(GECMIS_SINIRI)
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
