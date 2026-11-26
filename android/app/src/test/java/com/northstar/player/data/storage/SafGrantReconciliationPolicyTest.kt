package com.northstar.player.data.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class SafGrantReconciliationPolicyTest {
    @Test fun newlyDisconnectedTreeIsMarkedUnavailable() {
        assertEquals(
            SafGrantReconciliationAction.MARK_UNAVAILABLE,
            SafGrantReconciliationPolicy.action(wasAvailable = true, isAvailable = false)
        )
    }

    @Test fun persistentlyUnavailableTreeRemainsHiddenWithoutRescanning() {
        assertEquals(
            SafGrantReconciliationAction.MARK_UNAVAILABLE,
            SafGrantReconciliationPolicy.action(wasAvailable = false, isAvailable = false)
        )
    }

    @Test fun reconnectedTreeIsReindexedToRestoreItsVisibleSnapshot() {
        assertEquals(
            SafGrantReconciliationAction.RESCAN,
            SafGrantReconciliationPolicy.action(wasAvailable = false, isAvailable = true)
        )
    }

    @Test fun continuouslyMountedTreeIsNotRecursivelyRescannedByGrantCheck() {
        assertEquals(
            SafGrantReconciliationAction.NONE,
            SafGrantReconciliationPolicy.action(wasAvailable = true, isAvailable = true)
        )
    }
}
