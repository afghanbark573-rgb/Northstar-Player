package com.northstar.player.data.storage

import android.content.Intent
import com.northstar.player.core.database.ExternalTreeGrantEntity
import com.northstar.player.core.database.FolderEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SafFolderManagerTest {
    private val treeUri = "content://com.android.externalstorage.documents/tree/primary%3AMovies"
    private val sourceKey = SafSourceIdentity.key(treeUri)
    private val grant = ExternalTreeGrantEntity(
        treeUri = treeUri,
        displayName = "USB drive",
        persistedFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        grantedAtMillis = 10L,
        isAvailable = true
    )
    private val parent = FolderEntity(
        folderKey = "$sourceKey|primary:Movies/Clips",
        parentFolderKey = "$sourceKey|primary:Movies",
        displayName = "Clips",
        sourceKey = sourceKey,
        relativePath = "Movies/Clips",
        containsVideos = false,
        videoCount = 0,
        childFolderCount = 0,
        discoveredAtMillis = null,
        acknowledgedAtMillis = null
    )

    @Test fun createsDirectoryInsideTheSelectedNestedFolder() = runBlocking {
        val provider = FakeProvider(grant)
        val result = SafFolderManager(provider).createFolder(parent, "  Travel Clips  ")
        assertEquals(grant, result)
        assertEquals(treeUri, provider.createdTreeUri)
        assertEquals("primary:Movies/Clips", provider.createdParentId)
        assertEquals("Travel Clips", provider.createdName)
    }

    @Test fun refusesWhenNoMatchingPersistedGrantExists() = runBlocking {
        val provider = FakeProvider(null)
        assertFails<SecurityException> { SafFolderManager(provider).createFolder(parent, "New") }
        assertFalse(provider.createCalled)
    }

    @Test fun refusesWhenStorageIsUnavailable() = runBlocking {
        val provider = FakeProvider(grant.copy(isAvailable = false))
        assertFails<IOException> { SafFolderManager(provider).createFolder(parent, "New") }
        assertFalse(provider.createCalled)
    }

    @Test fun refusesWhenStoredGrantIsReadOnly() = runBlocking {
        val readOnly = grant.copy(persistedFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val provider = FakeProvider(readOnly)
        assertFails<SecurityException> { SafFolderManager(provider).createFolder(parent, "New") }
        assertFalse(provider.createCalled)
    }

    @Test fun refusesWhenAndroidNoLongerHoldsLiveWritePermission() = runBlocking {
        val provider = FakeProvider(grant).apply { liveReadWrite = false }
        assertFails<SecurityException> { SafFolderManager(provider).createFolder(parent, "New") }
        assertFalse(provider.createCalled)
    }

    @Test fun reportsProviderRefusalWithoutClaimingSuccess() = runBlocking {
        val provider = FakeProvider(grant).apply { createResult = false }
        assertFails<IOException> { SafFolderManager(provider).createFolder(parent, "New") }
        assertTrue(provider.createCalled)
    }

    @Test fun mapsUnsupportedProviderOperationToUsefulIoError() = runBlocking {
        val provider = FakeProvider(grant).apply { createError = UnsupportedOperationException("unsupported") }
        val error = assertFails<IOException> { SafFolderManager(provider).createFolder(parent, "New") }
        assertTrue(error.message!!.contains("does not support"))
    }

    @Test fun mapsProviderPermissionDenialToSecurityError() = runBlocking {
        val provider = FakeProvider(grant).apply { createError = SecurityException("denied") }
        val error = assertFails<SecurityException> { SafFolderManager(provider).createFolder(parent, "New") }
        assertTrue(error.message!!.contains("Write access was denied"))
    }

    @Test fun mapsProviderNameRejectionToUsefulIoError() = runBlocking {
        val provider = FakeProvider(grant).apply { createError = IllegalArgumentException("invalid") }
        val error = assertFails<IOException> { SafFolderManager(provider).createFolder(parent, "New") }
        assertTrue(error.message!!.contains("rejected this folder name"))
    }

    @Test fun rejectsNonSafFolderAndMismatchedFolderIdentity() = runBlocking {
        val provider = FakeProvider(grant)
        val manager = SafFolderManager(provider)
        assertFails<IllegalArgumentException> {
            manager.createFolder(parent.copy(sourceKey = "mediastore:internal"), "New")
        }
        assertFails<IllegalArgumentException> {
            manager.createFolder(parent.copy(folderKey = "saf:wrong|document"), "New")
        }
        assertFalse(provider.createCalled)
    }

    @Test fun rejectsUnsafeNamesBeforeProviderCall() = runBlocking {
        val provider = FakeProvider(grant)
        val manager = SafFolderManager(provider)
        listOf("", "  ", ".", "..", "one/two", "one\\two", "line\nbreak").forEach { name ->
            assertFails<IllegalArgumentException> { manager.createFolder(parent, name) }
        }
        assertFalse(provider.createCalled)
    }

    private class FakeProvider(
        private val grant: ExternalTreeGrantEntity?
    ) : SafFolderProvider {
        var liveReadWrite = true
        var createResult = true
        var createError: RuntimeException? = null
        var createCalled = false
        var createdTreeUri: String? = null
        var createdParentId: String? = null
        var createdName: String? = null

        override suspend fun findGrant(sourceKey: String): ExternalTreeGrantEntity? =
            grant?.takeIf { SafSourceIdentity.key(it.treeUri) == sourceKey }

        override fun hasPersistedReadWrite(treeUri: String): Boolean = liveReadWrite

        override fun createDirectory(treeUri: String, parentDocumentId: String, name: String): Boolean {
            createCalled = true
            createdTreeUri = treeUri
            createdParentId = parentDocumentId
            createdName = name
            createError?.let { throw it }
            return createResult
        }
    }

    private suspend inline fun <reified T : Throwable> assertFails(crossinline block: suspend () -> Unit): T {
        try {
            runBlocking { block() }
        } catch (error: Throwable) {
            if (error is T) return error
            throw AssertionError("Expected ${T::class.java.simpleName}, got ${error::class.java.simpleName}", error)
        }
        throw AssertionError("Expected ${T::class.java.simpleName} to be thrown")
    }
}
