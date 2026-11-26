package com.northstar.player.data.library

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Centralizes unique foreground-triggered and periodic local-library reconciliation work. */
class MediaLibraryScanScheduler(context: Context) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    fun ensurePeriodicScan() {
        val request = PeriodicWorkRequestBuilder<MediaLibraryScanWorker>(
            PERIODIC_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(localScanConstraints())
            .setInputData(Data.Builder().putBoolean(INPUT_FORCE_FULL, true).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            MediaLibraryScanWorker.UNIQUE_PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun enqueueRefresh() {
        val request = OneTimeWorkRequestBuilder<MediaLibraryScanWorker>()
            .setConstraints(localScanConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            MediaLibraryScanWorker.UNIQUE_REFRESH_WORK,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    private fun localScanConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .build()

    companion object {
        const val INPUT_FORCE_FULL = "force_full_reconciliation"
        private const val PERIODIC_INTERVAL_HOURS = 12L
    }
}
