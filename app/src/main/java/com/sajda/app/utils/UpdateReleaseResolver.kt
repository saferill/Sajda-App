package com.sajda.app.utils

import com.sajda.app.data.model.GithubAsset
import java.io.File
import java.security.MessageDigest

object UpdateReleaseResolver {
    private val checksumPattern = Regex("""sha256\s*[:=]\s*([A-Fa-f0-9]{64})""")

    fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(latestParts.size, currentParts.size)

        for (index in 0 until maxLength) {
            val latestValue = latestParts.getOrElse(index) { 0 }
            val currentValue = currentParts.getOrElse(index) { 0 }
            if (latestValue > currentValue) return true
            if (latestValue < currentValue) return false
        }

        return false
    }

    fun resolveChecksum(asset: GithubAsset, releaseNotes: String): String? {
        val digestValue = asset.digest
            ?.substringAfter("sha256:", asset.digest)
            ?.trim()
            ?.takeIf { it.length == 64 }
        if (digestValue != null) return digestValue
        return checksumPattern.find(releaseNotes)?.groupValues?.getOrNull(1)
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
