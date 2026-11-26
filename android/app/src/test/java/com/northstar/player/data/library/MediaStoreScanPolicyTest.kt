package com.northstar.player.data.library

import com.northstar.player.core.database.ScanStateEntity
import com.northstar.player.core.permissions.VideoPermissionScope
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaStoreScanPolicyTest {
    private fun state(
        scope: String = VideoPermissionScope.FULL.name,
        version: String? = "volume-v1",
        generation: Long? = 40L
    ) = ScanStateEntity(
        sourceKey = "mediastore:external",
        lastSuccessfulScanMillis = 100L,
        permissionScope = scope,
        inProgressSinceMillis = null,
        lastError = null,
        mediaStoreVersion = version,
        mediaStoreGeneration = generation
    )

    @Test fun fullPermissionAndMatchingCheckpointUseIncrementalScan() {
        assertTrue(MediaStoreScanPolicy.shouldUseIncremental(
            35, VideoPermissionScope.FULL, state(), "volume-v1", 45L, false
        ))
    }

    @Test fun explicitPeriodicReconciliationForcesFullSnapshot() {
        assertFalse(MediaStoreScanPolicy.shouldUseIncremental(
            35, VideoPermissionScope.FULL, state(), "volume-v1", 45L, true
        ))
    }

    @Test fun partialPermissionAlwaysUsesFullAccessibleSnapshot() {
        assertFalse(MediaStoreScanPolicy.shouldUseIncremental(
            35, VideoPermissionScope.PARTIAL, state(), "volume-v1", 45L, false
        ))
    }

    @Test fun permissionReselectionFromPreviousPartialScopeUsesFullSnapshot() {
        assertFalse(MediaStoreScanPolicy.shouldUseIncremental(
            35, VideoPermissionScope.FULL,
            state(scope = VideoPermissionScope.PARTIAL.name), "volume-v1", 45L, false
        ))
    }

    @Test fun mediaStoreVersionChangeInvalidatesCheckpoint() {
        assertFalse(MediaStoreScanPolicy.shouldUseIncremental(
            35, VideoPermissionScope.FULL, state(), "volume-v2", 45L, false
        ))
    }

    @Test fun missingCheckpointOrUnsupportedAndroidUsesFullSnapshot() {
        assertFalse(MediaStoreScanPolicy.shouldUseIncremental(
            35, VideoPermissionScope.FULL, state(generation = null), "volume-v1", 45L, false
        ))
        assertFalse(MediaStoreScanPolicy.shouldUseIncremental(
            29, VideoPermissionScope.FULL, state(), "volume-v1", null, false
        ))
    }

    @Test fun generationRollbackInvalidatesCheckpoint() {
        assertFalse(MediaStoreScanPolicy.shouldUseIncremental(
            35, VideoPermissionScope.FULL, state(), "volume-v1", 39L, false
        ))
    }
}
