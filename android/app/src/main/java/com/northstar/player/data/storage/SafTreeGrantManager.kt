package com.northstar.player.data.storage

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import com.northstar.player.core.database.ExternalTreeGrantDao
import com.northstar.player.core.database.ExternalTreeGrantEntity

/**
 * Owns persistable SAF permissions and the Room record describing each selected tree.
 * The actual tree URI is kept locally because Android requires it to reopen the provider.
 */
class SafTreeGrantManager(
    private val resolver: ContentResolver,
    private val dao: ExternalTreeGrantDao,
    private val clock: () -> Long = System::currentTimeMillis
) {
    suspend fun addTree(treeUri: Uri, displayName: String): ExternalTreeGrantEntity {
        val read = Intent.FLAG_GRANT_READ_URI_PERMISSION
        val write = Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        // Prefer read/write so provider-backed folder creation can be supported later.
        // Some providers grant read-only access, so fall back without losing that valid access.
        try {
            resolver.takePersistableUriPermission(treeUri, read or write)
        } catch (_: SecurityException) {
            resolver.takePersistableUriPermission(treeUri, read)
        }

        val persisted = resolver.persistedUriPermissions.firstOrNull { it.uri == treeUri }
        if (persisted == null || !persisted.isReadPermission) {
            throw SecurityException("Android did not retain read access to this folder")
        }

        val actualFlags = (if (persisted.isReadPermission) read else 0) or
            (if (persisted.isWritePermission) write else 0)
        val grant = ExternalTreeGrantEntity(
            treeUri = treeUri.toString(),
            displayName = displayName.ifBlank { "External storage" },
            persistedFlags = actualFlags,
            grantedAtMillis = clock(),
            isAvailable = true
        )
        dao.upsert(grant)
        return grant
    }

    suspend fun refreshGrantAvailability(): List<ExternalTreeGrantEntity> {
        val persisted = resolver.persistedUriPermissions
            .filter { it.isReadPermission }
            .map { it.uri.toString() }
            .toSet()
        dao.all().forEach { grant ->
            dao.setAvailability(grant.treeUri, grant.treeUri in persisted)
        }
        return dao.all()
    }

    suspend fun removeTree(grant: ExternalTreeGrantEntity) {
        val uri = Uri.parse(grant.treeUri)
        try {
            resolver.releasePersistableUriPermission(uri, grant.persistedFlags)
        } catch (_: SecurityException) {
            // The system permission may already have been revoked.
        }
        dao.delete(grant.treeUri)
    }
}
