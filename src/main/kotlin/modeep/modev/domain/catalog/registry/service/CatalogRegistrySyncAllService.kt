package modeep.modev.domain.catalog.registry.service

import modeep.modev.domain.catalog.controller.dto.response.registry.CatalogRegistrySyncAllResponse
import modeep.modev.domain.catalog.registry.vo.CatalogRegistryTargetType
import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import org.springframework.stereotype.Service

@Service
class CatalogRegistrySyncAllService(
    private val techStackRepository: TechStackRepository,
    private val dependencyRepository: DependencyRepository,
    private val catalogRegistrySyncService: CatalogRegistrySyncService,
    private val catalogRegistrySyncFailureService: CatalogRegistrySyncFailureService,
) {
    // 카탈로그 전체 동기화
    fun syncAll(): CatalogRegistrySyncAllResponse {
        var syncedCount = 0
        var failedCount = 0

        techStackRepository.findByRegistryAutoSyncTrueAndRegistryTypeIsNotNullAndRegistryIdentifierIsNotNull()
            .forEach { techStack ->
                try {
                    catalogRegistrySyncService.sync(CatalogRegistryTargetType.TECH_STACK, techStack.publicId)
                    syncedCount++
                } catch (e: Exception) {
                    failedCount++
                    runCatching {
                        catalogRegistrySyncFailureService.recordTechStackFailure(techStack.publicId, e)
                    }
                }
            }

        dependencyRepository.findByRegistryAutoSyncTrueAndRegistryTypeIsNotNullAndRegistryIdentifierIsNotNull()
            .forEach { dependency ->
                try {
                    catalogRegistrySyncService.sync(CatalogRegistryTargetType.DEPENDENCY, dependency.publicId)
                    syncedCount++
                } catch (e: Exception) {
                    failedCount++
                    runCatching {
                        catalogRegistrySyncFailureService.recordDependencyFailure(dependency.publicId, e)
                    }
                }
            }

        return CatalogRegistrySyncAllResponse(
            syncedCount = syncedCount,
            failedCount = failedCount,
        )
    }
}
