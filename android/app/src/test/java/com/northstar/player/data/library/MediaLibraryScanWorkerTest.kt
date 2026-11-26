package com.northstar.player.data.library

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MediaLibraryScanWorkerTest {
    @Test fun cancellationIsRethrownInsteadOfConvertedIntoRetryableFailure() = runBlocking {
        val cancellation = CancellationException("WorkManager stopped this worker")

        val thrown = try {
            runSafReconciliation { throw cancellation }
            null
        } catch (error: CancellationException) {
            error
        }

        assertSame(cancellation, thrown)
    }

    @Test fun ordinaryProviderFailureIsReturnedForWorkerRetryHandling() = runBlocking {
        val failure = IOException("Storage provider temporarily unavailable")

        val result = runSafReconciliation<Boolean> { throw failure }

        assertTrue(result.isFailure)
        assertSame(failure, result.exceptionOrNull())
    }
}
