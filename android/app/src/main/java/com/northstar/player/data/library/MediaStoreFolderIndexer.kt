package com.northstar.player.data.library

import com.northstar.player.core.database.FolderEntity
import com.northstar.player.core.database.MediaItemEntity

/**
 * Builds a stable folder index from MediaStore RELATIVE_PATH values.
 *
 * MediaStore only reports directories containing indexed media. Empty directories must be
 * discovered through a separately granted SAF tree; this index deliberately does not invent them.
 */
object MediaStoreFolderIndexer {
    fun build(items: List<MediaItemEntity>, sourceKey: String, discoveredAtMillis: Long? = null): List<FolderEntity> {
        data class MutableFolder(val path: String, val parentPath: String?, val name: String, var directVideos: Int = 0, var totalVideos: Int = 0, val children: MutableSet<String> = linkedSetOf())
        val folders = linkedMapOf<String, MutableFolder>()
        val relevant = items.filter { it.sourceKey == sourceKey && it.isAvailable }
        relevant.forEach { item ->
            val segments = item.relativePath.orEmpty().replace('\\', '/').split('/').filter { it.isNotBlank() }
            var parent: String? = null
            val ancestors = mutableListOf<String>()
            segments.forEach { segment ->
                val path = if (parent == null) segment else "$parent/$segment"
                folders.getOrPut(path) { MutableFolder(path, parent, segment) }
                if (parent != null) folders[parent]?.children?.add(path)
                parent = path
                ancestors += path
            }
            if (ancestors.isNotEmpty()) {
                val leaf = folders.getValue(ancestors.last())
                leaf.directVideos++
                ancestors.forEach { path -> folders.getValue(path).totalVideos++ }
            }
        }
        return folders.values.sortedBy { it.path.lowercase() }.map { folder ->
            FolderEntity(
                folderKey = "$sourceKey|${folder.path}",
                parentFolderKey = folder.parentPath?.let { "$sourceKey|$it" },
                displayName = folder.name, sourceKey = sourceKey, relativePath = folder.path,
                containsVideos = folder.directVideos > 0, videoCount = folder.totalVideos,
                childFolderCount = folder.children.size, discoveredAtMillis = discoveredAtMillis,
                acknowledgedAtMillis = null
            )
        }
    }
}