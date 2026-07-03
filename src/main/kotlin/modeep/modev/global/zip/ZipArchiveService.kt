package modeep.modev.global.zip

import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Component
class ZipArchiveService {
    fun create(entries: List<ZipArchiveEntry>): ByteArray {
        val output = ByteArrayOutputStream()

        ZipOutputStream(output).use { zip ->
            entries.forEach { entry ->
                val entryName = sanitizeEntryPath(entry.path) ?: return@forEach

                val zipEntryName =
                    if (entry.type == ZipArchiveEntryType.DIRECTORY) {
                        "$entryName/"
                    } else {
                        entryName
                    }

                zip.putNextEntry(ZipEntry(zipEntryName))
                if (entry.type == ZipArchiveEntryType.FILE) {
                    zip.write(entry.content.orEmpty().toByteArray(Charsets.UTF_8))
                }
                zip.closeEntry()
            }
        }

        return output.toByteArray()
    }

    private fun sanitizeEntryPath(rawPath: String): String? {
        val normalized = rawPath.replace('\\', '/').trim().trim('/')

        if (normalized.isBlank()) return null

        val segments = normalized.split('/')

        require(segments.none { it.isBlank() || it == "." || it == ".." }) {
            "Invalid zip entry path: $rawPath"
        }

        return segments.joinToString("/")
    }
}
