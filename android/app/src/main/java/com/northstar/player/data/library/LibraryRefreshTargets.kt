package com.northstar.player.data.library

import com.northstar.player.core.database.ExternalTreeGrantEntity
import com.northstar.player.data.storage.SafSourceIdentity

/** Selects only the SAF trees relevant to the page being refreshed. */
object LibraryRefreshTargets {
    fun select(
        currentFolderSourceKey: String?,
        showingExternalStorage: Boolean,
        grants: List<ExternalTreeGrantEntity>
    ): List<ExternalTreeGrantEntity> {
        val available = grants.filter { it.isAvailable }
        return when {
            showingExternalStorage || currentFolderSourceKey == null -> available
            currentFolderSourceKey.startsWith("saf:") ->
                available.filter { SafSourceIdentity.key(it.treeUri) == currentFolderSourceKey }
            else -> emptyList()
        }
    }
}
