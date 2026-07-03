package modeep.modev.domain.catalog.controller.dto.response.registry

data class CatalogRegistrySyncAllResponse(
    val syncedCount: Int,
    val failedCount: Int,
)
