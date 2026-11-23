package com.northstar.player.data.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SafSourceIdentityTest {
    @Test fun sameTreeUriProducesStableSourceKey() {
        assertEquals(
            SafSourceIdentity.key("content://provider/tree/primary%3AMovies"),
            SafSourceIdentity.key("content://provider/tree/primary%3AMovies")
        )
    }

    @Test fun differentTreeUrisProduceDifferentSourceKeys() {
        assertNotEquals(
            SafSourceIdentity.key("content://provider/tree/primary%3AMovies"),
            SafSourceIdentity.key("content://provider/tree/ABCD-1234%3AMovies")
        )
    }

    @Test fun sourceKeyDoesNotContainRawTreeUri() {
        val uri = "content://private-provider/tree/serial-number"
        val key = SafSourceIdentity.key(uri)
        assertEquals(false, key.contains("private-provider"))
        assertEquals(68, key.length)
    }
}
