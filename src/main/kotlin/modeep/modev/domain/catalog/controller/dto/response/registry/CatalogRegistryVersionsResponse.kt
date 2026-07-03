package modeep.modev.domain.catalog.controller.dto.response.registry

import modeep.modev.domain.catalog.entity.vo.RegistryType
import modeep.modev.domain.catalog.registry.vo.CatalogRegistryTargetType
import java.time.Instant

data class CatalogRegistryVersionsResponse(
    val targetType: CatalogRegistryTargetType,
    val publicId: String,
    val registryType: RegistryType,
    val registryIdentifier: String,
    val latestVersion: String?,
    val versions: List<String>,
    val fetchedAt: Instant,
)
