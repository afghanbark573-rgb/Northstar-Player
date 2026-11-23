package com.northstar.player.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SafFolderNameTest {
    @Test fun trimsSurroundingWhitespace() {
        assertEquals("Travel Clips", SafFolderName.validate("  Travel Clips  "))
    }

    @Test fun rejectsBlankNames() {
        assertRejected("   ")
    }

    @Test fun rejectsDotPathSegments() {
        assertRejected(".")
        assertRejected("..")
    }

    @Test fun rejectsPathSeparators() {
        assertRejected("Movies/2026")
        assertRejected("Movies\\2026")
    }

    @Test fun rejectsControlCharacters() {
        assertRejected("New\nFolder")
    }

    private fun assertRejected(value: String) {
        try {
            SafFolderName.validate(value)
            fail("Expected invalid folder name to be rejected: $value")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }
}
