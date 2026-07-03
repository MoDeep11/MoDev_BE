package modeep.modev.domain.catalog.registry.service

import modeep.modev.domain.catalog.controller.dto.response.registry.CatalogRegistrySyncResponse
import modeep.modev.domain.catalog.controller.dto.response.registry.CatalogRegistryVersionsResponse
import modeep.modev.domain.catalog.entity.CatalogRegistryVersion
import modeep.modev.domain.catalog.entity.Dependency
import modeep.modev.domain.catalog.entity.TechStack
import modeep.modev.domain.catalog.entity.id.CatalogRegistryVersionId
import modeep.modev.domain.catalog.registry.util.RegistryVersionSelector
import modeep.modev.domain.catalog.registry.vo.CatalogRegistryTargetType
import modeep.modev.domain.catalog.repository.CatalogRegistryVersionRepository
import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.RegistryErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CatalogRegistrySyncWriter(
    private val techStackRepository: TechStackRepository,
    private val dependencyRepository: DependencyRepository,
    private val catalogRegistryVersionRepository: CatalogRegistryVersionRepository,
) {
    // sync() 데이터베이스 쓰기 담당
    @Transactional
    fun save(fetched: CatalogRegistryVersionsResponse): CatalogRegistrySyncResponse {
        val latestVersion =
            fetched.latestVersion
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw BusinessException(RegistryErrorCode.VERSION_NOT_FOUND)
        val targetId = findTargetId(fetched.targetType, fetched.publicId)

        saveFetchedVersions(targetId, fetched, latestVersion)
        recordSyncSuccess(fetched.targetType, fetched.publicId, fetched.fetchedAt)

        return CatalogRegistrySyncResponse(
            targetType = fetched.targetType,
            publicId = fetched.publicId,
            version = latestVersion,
            syncedAt = fetched.fetchedAt,
        )
    }

    private fun saveFetchedVersions(
        targetId: Long,
        fetched: CatalogRegistryVersionsResponse,
        latestVersion: String,
    ) {
        val fetchedVersions =
            fetched.versions
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()

        if (fetchedVersions.isEmpty()) {
            throw BusinessException(RegistryErrorCode.VERSION_NOT_FOUND)
        }

        if (latestVersion !in fetchedVersions) {
            throw BusinessException(RegistryErrorCode.VERSION_NOT_FOUND)
        }

        val existingVersions =
            catalogRegistryVersionRepository.findByIdTargetTypeAndIdTargetId(
                targetType = fetched.targetType,
                targetId = targetId,
            )
        val existingByVersion = existingVersions.associateBy { it.version }

        val fetchedEntities =
            fetchedVersions.map { version ->
                existingByVersion[version]?.apply {
                    markLatest(
                        latest = version == latestVersion,
                        fetchedAt = fetched.fetchedAt,
                    )
                } ?: CatalogRegistryVersion(
                    id =
                        CatalogRegistryVersionId(
                            targetType = fetched.targetType,
                            targetId = targetId,
                            version = version,
                        ),
                    isLatest = version == latestVersion,
                    isStable = RegistryVersionSelector.isStable(version),
                    fetchedAt = fetched.fetchedAt,
                )
            }
        val staleLatestVersions =
            existingVersions
                .filter { it.version !in fetchedVersions && it.isLatest }
                .onEach {
                    it.markLatest(
                        latest = false,
                        fetchedAt = fetched.fetchedAt,
                    )
                }

        catalogRegistryVersionRepository.saveAll(fetchedEntities + staleLatestVersions)
    }

    private fun recordSyncSuccess(
        targetType: CatalogRegistryTargetType,
        publicId: String,
        syncedAt: Instant,
    ) {
        when (targetType) {
            CatalogRegistryTargetType.TECH_STACK ->
                findTechStack(publicId)
                    .also {
                        it.recordRegistrySyncSuccess(syncedAt)
                        techStackRepository.save(it)
                    }
            CatalogRegistryTargetType.DEPENDENCY ->
                findDependency(publicId)
                    .also {
                        it.recordRegistrySyncSuccess(syncedAt)
                        dependencyRepository.save(it)
                    }
        }
    }

    private fun findTargetId(
        targetType: CatalogRegistryTargetType,
        publicId: String,
    ): Long =
        when (targetType) {
            CatalogRegistryTargetType.TECH_STACK ->
                requireNotNull(findTechStack(publicId).id) {
                    "TechStack id must not be null: $publicId"
                }
            CatalogRegistryTargetType.DEPENDENCY ->
                requireNotNull(findDependency(publicId).id) {
                    "Dependency id must not be null: $publicId"
                }
        }

    private fun findTechStack(publicId: String): TechStack =
        techStackRepository.findByPublicId(publicId)
            ?: throw BusinessException(RegistryErrorCode.TARGET_NOT_FOUND)

    private fun findDependency(publicId: String): Dependency =
        dependencyRepository.findByPublicId(publicId)
            ?: throw BusinessException(RegistryErrorCode.TARGET_NOT_FOUND)
}
