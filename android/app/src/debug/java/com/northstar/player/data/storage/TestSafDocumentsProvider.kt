package com.northstar.player.data.storage

import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import java.io.FileNotFoundException

/**
 * Debug-only in-memory DocumentsProvider used by instrumentation tests.
 * It lives in the debug APK so the provider shares the already-started target-app
 * process instead of requiring Android to launch a second test-APK provider process.
 */
class TestSafDocumentsProvider : DocumentsProvider() {
    private data class Document(
        val id: String,
        val name: String,
        val mimeType: String,
        val parentId: String?
    )

    private val documents = linkedMapOf<String, Document>()

    override fun onCreate(): Boolean {
        synchronized(documents) {
            documents.clear()
            documents[ROOT_ID] = Document(ROOT_ID, "Test storage", DocumentsContract.Document.MIME_TYPE_DIR, null)
            documents[PARENT_ID] = Document(PARENT_ID, "Nested parent", DocumentsContract.Document.MIME_TYPE_DIR, ROOT_ID)
        }
        return true
    }

    override fun queryRoots(projection: Array<out String>?): Cursor {
        val columns = projection ?: ROOT_COLUMNS
        return MatrixCursor(columns).apply {
            val values = mapOf(
                DocumentsContract.Root.COLUMN_ROOT_ID to ROOT_ID,
                DocumentsContract.Root.COLUMN_FLAGS to DocumentsContract.Root.FLAG_SUPPORTS_CREATE,
                DocumentsContract.Root.COLUMN_TITLE to "Northstar test storage",
                DocumentsContract.Root.COLUMN_DOCUMENT_ID to ROOT_ID,
                DocumentsContract.Root.COLUMN_MIME_TYPES to DocumentsContract.Document.MIME_TYPE_DIR
            )
            addRow(columns.map { values[it] })
        }
    }

    override fun queryDocument(documentId: String, projection: Array<out String>?): Cursor {
        val columns = projection ?: DOCUMENT_COLUMNS
        val doc = synchronized(documents) { documents[documentId] }
            ?: throw FileNotFoundException("Unknown document: $documentId")
        return MatrixCursor(columns).apply { addRow(documentValues(doc, columns)) }
    }

    override fun queryChildDocuments(
        parentDocumentId: String,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val columns = projection ?: DOCUMENT_COLUMNS
        synchronized(documents) {
            if (documents[parentDocumentId]?.mimeType != DocumentsContract.Document.MIME_TYPE_DIR) {
                throw FileNotFoundException("Parent is not a directory: $parentDocumentId")
            }
            return MatrixCursor(columns).apply {
                documents.values.filter { it.parentId == parentDocumentId }
                    .forEach { addRow(documentValues(it, columns)) }
            }
        }
    }

    override fun createDocument(parentDocumentId: String, mimeType: String, displayName: String): String {
        synchronized(documents) {
            val parent = documents[parentDocumentId]
                ?: throw FileNotFoundException("Parent directory does not exist")
            if (parent.mimeType != DocumentsContract.Document.MIME_TYPE_DIR) {
                throw FileNotFoundException("Parent is not a directory")
            }
            if (mimeType != DocumentsContract.Document.MIME_TYPE_DIR) {
                throw UnsupportedOperationException("Test provider only creates directories")
            }
            if (documents.values.any { it.parentId == parentDocumentId && it.name == displayName }) {
                throw IllegalArgumentException("A document with this name already exists")
            }
            val id = "created-${System.nanoTime()}"
            documents[id] = Document(id, displayName, mimeType, parentDocumentId)
            return DocumentsContract.buildDocumentUri(AUTHORITY, id).toString()
        }
    }

    override fun deleteDocument(documentId: String) {
        synchronized(documents) {
            documents.remove(documentId)
        }
    }

    override fun getDocumentType(documentId: String): String =
        synchronized(documents) { documents[documentId]?.mimeType }
            ?: throw FileNotFoundException("Unknown document: $documentId")

    override fun openDocument(documentId: String, mode: String, signal: CancellationSignal?): ParcelFileDescriptor {
        throw FileNotFoundException("The test provider does not expose file contents")
    }

    private fun documentValues(document: Document, columns: Array<out String>): Array<Any?> {
        val values = mapOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID to document.id,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME to document.name,
            DocumentsContract.Document.COLUMN_MIME_TYPE to document.mimeType,
            DocumentsContract.Document.COLUMN_FLAGS to DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE,
            DocumentsContract.Document.COLUMN_SIZE to 0L,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED to 0L
        )
        return columns.map { values[it] }.toTypedArray()
    }

    companion object {
        const val AUTHORITY = "com.northstar.player.test.documents"
        const val ROOT_ID = "test-root"
        const val PARENT_ID = "nested-parent"
        private val ROOT_COLUMNS = arrayOf(
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_MIME_TYPES
        )
        private val DOCUMENT_COLUMNS = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )
    }
}
