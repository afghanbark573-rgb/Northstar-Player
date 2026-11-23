package com.northstar.player.data.storage

import android.provider.DocumentsContract
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.northstar.player.NorthstarApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the production DocumentsContract adapter against an installed test provider. */
@RunWith(AndroidJUnit4::class)
class SafFolderProviderIntegrationTest {
    @Test
    fun createsAndListsDirectoryInsideNestedDocumentProviderFolder() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val resolver = instrumentation.context.contentResolver
        val targetContext = instrumentation.targetContext
        val app = targetContext.applicationContext as NorthstarApplication

        val treeUri = DocumentsContract.buildTreeDocumentUri(
            TestSafDocumentsProvider.AUTHORITY,
            TestSafDocumentsProvider.ROOT_ID
        )
        val adapter = AndroidSafFolderProvider(resolver, app.database.externalTreeGrantDao())
        val folderName = "Integration-${System.nanoTime()}"

        assertTrue(
            "DocumentsContract.createDocument should return a created document",
            adapter.createDirectory(
                treeUri.toString(),
                TestSafDocumentsProvider.PARENT_ID,
                folderName
            )
        )

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            TestSafDocumentsProvider.PARENT_ID
        )
        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ),
            null,
            null,
            null
        )!!.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            var found = false
            while (cursor.moveToNext()) {
                if (cursor.getString(nameColumn) == folderName) {
                    assertEquals(
                        DocumentsContract.Document.MIME_TYPE_DIR,
                        cursor.getString(mimeColumn)
                    )
                    found = true
                }
            }
            assertTrue("Created folder should appear in the provider's child listing", found)
        }
    }
}
