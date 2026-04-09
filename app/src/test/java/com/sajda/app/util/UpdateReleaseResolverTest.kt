package com.sajda.app.util

import com.sajda.app.data.model.GithubAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UpdateReleaseResolverTest {

    @Test
    fun semverComparison_detectsNewerVersion() {
        assertTrue(UpdateReleaseResolver.isNewerVersion("1.3.1", "1.3.0"))
        assertFalse(UpdateReleaseResolver.isNewerVersion("1.3.0", "1.3.0"))
        assertFalse(UpdateReleaseResolver.isNewerVersion("1.2.9", "1.3.0"))
    }

    @Test
    fun checksumResolver_prefersDigestField() {
        val asset = GithubAsset(
            name = "NurApp.apk",
            browser_download_url = "https://example.com/app.apk",
            size = 123,
            digest = "sha256:abc123"
        )

        assertEquals("abc123", UpdateReleaseResolver.resolveChecksum(asset, ""))
    }

    @Test
    fun sha256Hash_matchesExpectedLength() {
        val file = File.createTempFile("nurapp", ".txt")
        file.writeText("nurapp-test")

        try {
            val hash = UpdateReleaseResolver.sha256(file)
            assertEquals(64, hash.length)
        } finally {
            file.delete()
        }
    }
}
