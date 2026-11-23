package com.northstar.player.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SafSourceIdentityTest {
    @Test fun sameTreeUriProducesStableSourceKey() {
        assertEquals(
            SafSourceIdentity.key("content://provider/tree/primary%3AMovies"),
            SafSourceIdentity.key("content://provider/tree/primary%3AMovies")
        )
    }

    @Test fun differentTreeUrisProduceDifferentSourceKeys() {
        assertNotEquals(
            SafSourceIdentity.key("content://provider/tree/primary%3AMovies"),
            SafSourceIdentity.key("content://provider/tree/ABCD-1234%3AMovies")
        )
    }

    @Test fun sourceKeyDoesNotContainRawTreeUri() {
        val uri = "content://private-provider/tree/serial-number"
        val key = SafSourceIdentity.key(uri)
        assertEquals(false, key.contains("private-provider"))
        assertEquals(68, key.length)
    }

    @Test fun scanGenerationAdvancesPastFailedStagedAttempt() {
        assertEquals(2_001L, SafScanGeneration.next(
            clockMillis = 1_500L,
            lastSuccessfulScanMillis = 1_000L,
            latestIndexedAtMillis = 2_000L
        ))
    }

    @Test fun scanGenerationUsesCurrentClockWhenItIsNewest() {
        assertEquals(5_000L, SafScanGeneration.next(
            clockMillis = 5_000L,
            lastSuccessfulScanMillis = 1_000L,
            latestIndexedAtMillis = 2_000L
        ))
    }

    @Test fun scanGenerationAlwaysAdvancesPastLastSuccess() {
        assertEquals(3_001L, SafScanGeneration.next(
            clockMillis = 2_000L,
            lastSuccessfulScanMillis = 3_000L,
            latestIndexedAtMillis = 2_500L
        ))
    }
}
