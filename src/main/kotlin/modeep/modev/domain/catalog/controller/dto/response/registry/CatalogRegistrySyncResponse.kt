package modeep.modev.domain.catalog.controller.dto.response.registry

import modeep.modev.domain.catalog.registry.vo.CatalogRegistryTargetType
import java.time.Instant

data class CatalogRegistrySyncResponse(
    val targetType: CatalogRegistryTargetType,
    val publicId: String,
    val version: String?,
    val syncedAt: Instant?,
)
