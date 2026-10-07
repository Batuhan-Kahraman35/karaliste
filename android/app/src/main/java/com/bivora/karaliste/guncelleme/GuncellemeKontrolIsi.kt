package com.bivora.karaliste.guncelleme

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.util.concurrent.TimeUnit

// Günde bir kez yeni sürüm kontrol edilir; varsa bildirim gösterilir
class GuncellemeKontrolIsi(context: Context, parametreler: WorkerParameters) :
    CoroutineWorker(context, parametreler) {

    override suspend fun doWork(): Result {
        val bilgi = try {
            GuncellemeYoneticisi.al(applicationContext).kontrolEt()
        } catch (e: IOException) {
            return Result.retry()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.success()
        }

        if (bilgi != null) {
            Bildirimler.guncellemeVar(applicationContext, bilgi)
        }
        return Result.success()
    }

    companion object {
        private const val AD = "guncelleme_kontrol"

        fun planla(context: Context) {
            val istek = PeriodicWorkRequestBuilder<GuncellemeKontrolIsi>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(AD, ExistingPeriodicWorkPolicy.KEEP, istek)
        }
    }
}
