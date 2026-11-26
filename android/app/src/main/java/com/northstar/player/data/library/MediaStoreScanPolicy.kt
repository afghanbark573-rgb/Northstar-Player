package com.northstar.player.data.library

import com.northstar.player.core.database.ScanStateEntity
import com.northstar.player.core.permissions.VideoPermissionScope

/** Pure gate for deciding whether a generation delta is safe for this reconciliation. */
internal object MediaStoreScanPolicy {
    fun shouldUseIncremental(
        apiLevel: Int,
        permissionScope: VideoPermissionScope,
        previousState: ScanStateEntity?,
        currentVersion: String?,
        currentGeneration: Long?,
        forceFull: Boolean
    ): Boolean {
        if (forceFull || apiLevel < 30) return false
        if (permissionScope != VideoPermissionScope.FULL) return false
        if (previousState?.permissionScope != VideoPermissionScope.FULL) return false
        if (currentVersion.isNullOrBlank() || currentGeneration == null) return false
        if (previousState.mediaStoreVersion != currentVersion) return false
        val previousGeneration = previousState.mediaStoreGeneration ?: return false
        return previousGeneration >= 0L && currentGeneration >= previousGeneration
    }
}
