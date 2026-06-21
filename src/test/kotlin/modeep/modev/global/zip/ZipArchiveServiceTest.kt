package modeep.modev.global.zip

import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZipArchiveServiceTest {
    private val service = ZipArchiveService()

    @Test
    fun `creates zip with files and directories`() {
        val zip =
            service.create(
                listOf(
                    ZipArchiveEntry(
                        path = "backend",
                        type = ZipArchiveEntryType.DIRECTORY,
                    ),
                    ZipArchiveEntry(
                        path = "backend/build.gradle",
                        type = ZipArchiveEntryType.FILE,
                        content = "plugins {}",
                    ),
                    ZipArchiveEntry(
                        path = "/README.md",
                        type = ZipArchiveEntryType.FILE,
                        content = "# test",
                    ),
                ),
            )

        val entries = unzip(zip)

        assertTrue(entries.containsKey("backend/"))
        assertEquals("plugins {}", entries["backend/build.gradle"])
        assertEquals("# test", entries["README.md"])
    }

    @Test
    fun `skips blank paths`() {
        val zip =
            service.create(
                listOf(
                    ZipArchiveEntry(
                        path = " / ",
                        type = ZipArchiveEntryType.FILE,
                        content = "ignored",
                    ),
                    ZipArchiveEntry(
                        path = "README.md",
                        type = ZipArchiveEntryType.FILE,
                        content = "# test",
                    ),
                ),
            )

        val entries = unzip(zip)

        assertEquals(setOf("README.md"), entries.keys)
    }

    private fun unzip(zip: ByteArray): Map<String, String> {
        val entries = linkedMapOf<String, String>()

        ZipInputStream(ByteArrayInputStream(zip)).use { input ->
            var entry = input.nextEntry
            while (entry != null) {
                entries[entry.name] =
                    if (entry.isDirectory) {
                        ""
                    } else {
                        input.readBytes().toString(Charsets.UTF_8)
                    }
                input.closeEntry()
                entry = input.nextEntry
            }
        }

        return entries
    }
}
