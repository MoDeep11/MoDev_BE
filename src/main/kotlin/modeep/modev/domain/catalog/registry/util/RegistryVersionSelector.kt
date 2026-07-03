package modeep.modev.domain.catalog.registry.util

import java.math.BigInteger

object RegistryVersionSelector {
    private val unstableKeywords = listOf("snapshot", "alpha", "beta", "rc", "milestone")
    private val versionTokenRegex = Regex("""\d+|[A-Za-z]+""")

    fun isStable(version: String): Boolean {
        val normalized = version.lowercase()
        return unstableKeywords.none { normalized.contains(it) }
    }

    // 최신 안정 버전 확인
    fun latestStable(versions: List<String>): String? {
        val sortedVersions = versions.sortedWith { left, right -> compareVersion(right, left) }

        return sortedVersions.firstOrNull { isStable(it) } ?: sortedVersions.firstOrNull()
    }

    private fun compareVersion(
        left: String,
        right: String,
    ): Int {
        val leftTokens = left.toVersionTokens()
        val rightTokens = right.toVersionTokens()
        val maxSize = maxOf(leftTokens.size, rightTokens.size)

        for (index in 0 until maxSize) {
            val leftToken = leftTokens.getOrNull(index)
            val rightToken = rightTokens.getOrNull(index)

            if (leftToken == null) return compareMissingToken(rightTokens.drop(index))
            if (rightToken == null) return -compareMissingToken(leftTokens.drop(index))

            val tokenCompare = compareToken(leftToken, rightToken)
            if (tokenCompare != 0) {
                return tokenCompare
            }
        }

        return 0
    }

    private fun String.toVersionTokens(): List<String> =
        trim()
            .removePrefix("v")
            .removePrefix("V")
            .let { versionTokenRegex.findAll(it).map { match -> match.value }.toList() }

    private fun compareToken(
        left: String,
        right: String,
    ): Int {
        val leftNumber = left.toBigIntegerOrNull()
        val rightNumber = right.toBigIntegerOrNull()

        return when {
            leftNumber != null && rightNumber != null -> leftNumber.compareTo(rightNumber)
            leftNumber != null -> 1
            rightNumber != null -> -1
            else -> left.compareTo(right, ignoreCase = true)
        }
    }

    private fun compareMissingToken(remainingTokens: List<String>): Int {
        val firstRemainingToken = remainingTokens.firstOrNull() ?: return 0
        val firstRemainingNumber = firstRemainingToken.toBigIntegerOrNull()

        return when {
            firstRemainingNumber == null -> 1
            firstRemainingNumber == BigInteger.ZERO -> compareMissingToken(remainingTokens.drop(1))
            else -> -1
        }
    }
}
