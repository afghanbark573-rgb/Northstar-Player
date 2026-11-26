package com.northstar.player.data.library

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.northstar.player.NorthstarApplication
import com.northstar.player.core.permissions.VideoPermissionScope
import com.northstar.player.data.storage.SafGrantReconciliationAction
import com.northstar.player.data.storage.SafGrantReconciliationPolicy
import kotlinx.coroutines.CancellationException

/**
 * Converts ordinary reconciliation errors into a retry signal, but never consumes coroutine
 * cancellation. WorkManager cancellation must remain cooperative and stop this worker promptly.
 */
internal suspend fun <T> runSafReconciliation(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }

/**
 * Deferrable, process-safe reconciliation entry point. WorkManager owns retry and lifecycle.
 * SAF is reconciled without repeatedly walking every mounted tree: unavailable sources are
 * hidden, and a tree is fully re-indexed when it transitions back to reachable.
 */
class MediaLibraryScanWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as NorthstarApplication
        val scope = app.videoPermissionChecker.currentScope()

        val mediaStoreScan = app.mediaStoreVideoScanner.scan(scope)
        if (app.videoPermissionChecker.currentScope() != scope) return Result.retry()

        val safReconciliation = runSafReconciliation {
            val changes = app.safTreeGrantManager.refreshGrantAvailabilityChanges()
            var needsRetry = false
            changes.forEach { change ->
                when (SafGrantReconciliationPolicy.action(
                    wasAvailable = change.wasAvailable,
                    isAvailable = change.grant.isAvailable
                )) {
                    SafGrantReconciliationAction.MARK_UNAVAILABLE -> {
                        app.safTreeScanner.markUnavailable(
                            change.grant,
                            "External storage is disconnected or its folder permission is unavailable"
                        )
                    }
                    SafGrantReconciliationAction.RESCAN -> {
                        val result = app.safTreeScanner.scan(change.grant)
                        if (result.isFailure) {
                            app.safTreeScanner.markUnavailable(
                                change.grant,
                                "External storage could not be re-indexed: " +
                                    (result.exceptionOrNull()?.message ?: "provider unavailable")
                            )
                            needsRetry = true
                        }
                    }
                    SafGrantReconciliationAction.NONE -> Unit
                }
            }
            needsRetry
        }
        val safNeedsRetry = safReconciliation.getOrElse { true }

        if (safNeedsRetry) {
            return if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }

        return mediaStoreScan.fold(
            onSuccess = { Result.success() },
            onFailure = {
                // DENIED is a valid permission state after the scanner has reconciled the
                // visible set to empty. A permission change during this work is retried above.
                if (scope == VideoPermissionScope.DENIED) Result.success()
                else if (runAttemptCount < MAX_RETRIES) Result.retry()
                else Result.failure()
            }
        )
    }

    companion object {
        const val UNIQUE_REFRESH_WORK = "northstar.library.refresh"
        const val UNIQUE_PERIODIC_WORK = "northstar.library.periodic"
        private const val MAX_RETRIES = 3
    }
}
