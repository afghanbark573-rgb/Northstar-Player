package com.northstar.player.data.storage

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import com.northstar.player.core.database.ExternalTreeGrantDao
import com.northstar.player.core.database.ExternalTreeGrantEntity
import com.northstar.player.core.database.FolderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Validates a single provider folder name without interpreting paths or separators. */
object SafFolderName {
    fun validate(rawName: String): String {
        val name = rawName.trim()
        require(name.isNotEmpty()) { "Enter a folder name." }
        require(name != "." && name != "..") { "That folder name is not allowed." }
        require(name.none { it == '/' || it == '\\' || it.isISOControl() }) {
            "Folder names cannot contain path separators or control characters."
        }
        return name
    }
}

/**
 * Creates child directories only inside an indexed SAF tree for which Android still
 * holds a persisted write grant. The provider remains authoritative about support.
 */
class SafFolderManager(
    private val resolver: ContentResolver,
    private val grantDao: ExternalTreeGrantDao
) {
    suspend fun createFolder(parent: FolderEntity, rawName: String): ExternalTreeGrantEntity =
        withContext(Dispatchers.IO) {
            val name = SafFolderName.validate(rawName)
            require(parent.sourceKey.startsWith("saf:")) {
                "Folders can only be created inside selected USB/SD storage."
            }
            val separator = parent.folderKey.indexOf('|')
            require(separator > 0 && parent.folderKey.substring(0, separator) == parent.sourceKey) {
                "The selected folder identity is invalid."
            }
            val documentId = parent.folderKey.substring(separator + 1)
            require(documentId.isNotBlank()) { "The selected storage folder is invalid." }

            val grant = grantDao.all().firstOrNull {
                SafSourceIdentity.key(it.treeUri) == parent.sourceKey
            } ?: throw SecurityException("Select this storage folder again to restore access.")

            if (!grant.isAvailable) {
                throw IOException("This storage is unavailable. Reconnect it and refresh the library.")
            }
            val writeFlag = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            if ((grant.persistedFlags and writeFlag) == 0) {
                throw SecurityException("This storage was granted read-only access. Reselect it and allow write access.")
            }

            val treeUri = Uri.parse(grant.treeUri)
            val persisted = resolver.persistedUriPermissions.firstOrNull { it.uri == treeUri }
            if (persisted?.isReadPermission != true || !persisted.isWritePermission) {
                throw SecurityException("Android no longer has write permission for this folder. Reselect the storage.")
            }

            val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
            try {
                val created = DocumentsContract.createDocument(
                    resolver,
                    parentUri,
                    DocumentsContract.Document.MIME_TYPE_DIR,
                    name
                )
                if (created == null) {
                    throw IOException("The storage provider refused to create this folder.")
                }
                grant
            } catch (error: SecurityException) {
                throw SecurityException("Write access was denied by the storage provider. Reselect the folder and grant write access.", error)
            } catch (error: UnsupportedOperationException) {
                throw IOException("This storage provider does not support creating folders.", error)
            } catch (error: IllegalArgumentException) {
                throw IOException("The storage provider rejected this folder name.", error)
            }
        }
}
