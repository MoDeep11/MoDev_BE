package modeep.modev.domain.catalog.registry.service

import modeep.modev.domain.catalog.controller.dto.response.registry.CatalogRegistrySyncResponse
import modeep.modev.domain.catalog.controller.dto.response.registry.CatalogRegistryVersionsResponse
import modeep.modev.domain.catalog.entity.Dependency
import modeep.modev.domain.catalog.entity.TechStack
import modeep.modev.domain.catalog.entity.vo.RegistryType
import modeep.modev.domain.catalog.registry.client.RegistryClient
import modeep.modev.domain.catalog.registry.vo.CatalogRegistryTargetType
import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.RegistryErrorCode
import org.springframework.stereotype.Service

@Service
class CatalogRegistrySyncService(
    private val techStackRepository: TechStackRepository,
    private val dependencyRepository: DependencyRepository,
    private val catalogRegistrySyncWriter: CatalogRegistrySyncWriter,
    clients: List<RegistryClient>,
) {
    private val clientsByType =
        RegistryType.entries.associateWith { registryType ->
            clients.firstOrNull { it.supports(registryType) }
        }

    // 특정 기술 스택(TechStack)/의존성(Dependency)의 최신 버전 조회
    fun fetchVersions(
        targetType: CatalogRegistryTargetType,
        publicId: String,
    ): CatalogRegistryVersionsResponse {
        val target = findTarget(targetType, publicId)
        val registryType = target.registryType ?: throw BusinessException(RegistryErrorCode.METADATA_REQUIRED)
        val registryIdentifier = target.registryIdentifier ?: throw BusinessException(RegistryErrorCode.METADATA_REQUIRED)
        val result = clientFor(registryType).fetchVersions(registryIdentifier)

        return CatalogRegistryVersionsResponse(
            targetType = targetType,
            publicId = publicId,
            registryType = registryType,
            registryIdentifier = registryIdentifier,
            latestVersion = result.latestVersion,
            versions = result.versions,
            fetchedAt = result.fetchedAt,
        )
    }

    // 조회한 최신 버전 저장
    fun sync(
        targetType: CatalogRegistryTargetType,
        publicId: String,
    ): CatalogRegistrySyncResponse {
        val fetched = fetchVersions(targetType, publicId)

        return catalogRegistrySyncWriter.save(fetched)
    }

    // RegistryType에 정의되어있는 클라이언트를 가져옵니다
    private fun clientFor(registryType: RegistryType): RegistryClient =
        clientsByType[registryType]
            ?: throw BusinessException(
                errorCode = RegistryErrorCode.CLIENT_NOT_CONFIGURED,
                details = mapOf("registryType" to registryType),
            )

    // catalog id를 기반으로 DB에 저장되어있는 기술 스택 또는 의존성을 가져옵니다.
    private fun findTarget(
        targetType: CatalogRegistryTargetType,
        publicId: String,
    ): RegistryTarget =
        when (targetType) {
            CatalogRegistryTargetType.TECH_STACK ->
                findTechStack(publicId).let {
                    RegistryTarget(it.registryType, it.registryIdentifier)
                }
            CatalogRegistryTargetType.DEPENDENCY ->
                findDependency(publicId).let {
                    RegistryTarget(it.registryType, it.registryIdentifier)
                }
        }

    private fun findTechStack(publicId: String): TechStack =
        techStackRepository.findByPublicId(publicId)
            ?: throw BusinessException(RegistryErrorCode.TARGET_NOT_FOUND)

    private fun findDependency(publicId: String): Dependency =
        dependencyRepository.findByPublicId(publicId)
            ?: throw BusinessException(RegistryErrorCode.TARGET_NOT_FOUND)

    private data class RegistryTarget(
        val registryType: RegistryType?,
        val registryIdentifier: String?,
    )
}
