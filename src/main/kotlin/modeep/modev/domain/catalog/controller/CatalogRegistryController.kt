package modeep.modev.domain.catalog.controller

import modeep.modev.domain.catalog.registry.service.CatalogRegistrySyncAllService
import modeep.modev.domain.catalog.registry.service.CatalogRegistrySyncService
import modeep.modev.domain.catalog.registry.vo.CatalogRegistryTargetType
import modeep.modev.global.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/catalog/registry")
class CatalogRegistryController(
    private val catalogRegistrySyncService: CatalogRegistrySyncService,
    private val catalogRegistrySyncAllService: CatalogRegistrySyncAllService,
) : CatalogRegistryControllerDocs {
    @GetMapping("/versions")
    override fun getVersions(
        @RequestParam targetType: CatalogRegistryTargetType,
        @RequestParam publicId: String,
    ): ApiResponse =
        ApiResponse(
            success = true,
            data = catalogRegistrySyncService.fetchVersions(targetType, publicId),
        )

    @PostMapping("/sync")
    override fun sync(
        @RequestParam targetType: CatalogRegistryTargetType,
        @RequestParam publicId: String,
    ): ApiResponse =
        ApiResponse(
            success = true,
            data = catalogRegistrySyncService.sync(targetType, publicId),
        )

    @PostMapping("/sync/all")
    override fun syncAll(): ApiResponse =
        ApiResponse(
            success = true,
            data = catalogRegistrySyncAllService.syncAll(),
        )
}
