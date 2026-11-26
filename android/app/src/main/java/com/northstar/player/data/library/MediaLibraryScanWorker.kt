package com.northstar.player.data.library

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.northstar.player.NorthstarApplication
import com.northstar.player.core.permissions.VideoPermissionScope

/**
 * Deferrable, process-safe scan entry point. WorkManager owns retry and lifecycle handling.
 */
class MediaLibraryScanWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as NorthstarApplication
        val scope = app.videoPermissionChecker.currentScope()

        // Permission is user-controlled. Reconcile the visible library to an empty
        // accessible set while retaining local metadata; denial is not a retryable error.
        if (scope == VideoPermissionScope.DENIED) {
            app.mediaStoreVideoScanner.scan(scope)
            return if (app.videoPermissionChecker.currentScope() == scope) {
                Result.success()
            } else {
                Result.retry()
            }
        }

        val scan = app.mediaStoreVideoScanner.scan(scope)
        // If Android's selected-media set changed during the scan, do not treat the
        // just-published visibility set as current; retry against the new grant scope.
        if (app.videoPermissionChecker.currentScope() != scope) return Result.retry()

        return scan.fold(
            onSuccess = { Result.success() },
            onFailure = {
                if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
            }
        )
    }

    companion object {
        const val UNIQUE_REFRESH_WORK = "northstar.library.refresh"
        const val UNIQUE_PERIODIC_WORK = "northstar.library.periodic"
        private const val MAX_RETRIES = 3
    }
}
