package modeep.modev.domain.catalog.registry.util

object RegistryVersionSelector {
    private val unstableKeywords = listOf("snapshot", "alpha", "beta", "rc", "milestone")

    fun isStable(version: String): Boolean {
        val normalized = version.lowercase()
        return unstableKeywords.none { normalized.contains(it) }
    }

    // 최신 안정 버전 확인
    fun latestStable(versions: List<String>): String? = versions.firstOrNull { isStable(it) } ?: versions.firstOrNull()
}
