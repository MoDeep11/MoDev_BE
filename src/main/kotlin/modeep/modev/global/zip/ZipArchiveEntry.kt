package modeep.modev.global.zip

data class ZipArchiveEntry(
    val path: String,
    val type: ZipArchiveEntryType,
    val content: String? = null,
)

enum class ZipArchiveEntryType {
    FILE,
    DIRECTORY,
}
