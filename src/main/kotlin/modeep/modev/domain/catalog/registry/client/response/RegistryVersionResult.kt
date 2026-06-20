package modeep.modev.domain.catalog.registry.client.response

import java.time.Instant

data class RegistryVersionResult(
    val latestVersion: String?,
    val versions: List<String>,
    val fetchedAt: Instant = Instant.now(),
)
