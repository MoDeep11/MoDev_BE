package modeep.modev.domain.catalog.service

import modeep.modev.domain.catalog.controller.dto.response.DependencyResponse
import modeep.modev.domain.catalog.controller.dto.response.GetDependenciesResponse
import modeep.modev.domain.catalog.repository.DependencyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetDependenciesService(
    private val dependencyRepository: DependencyRepository,
) {
    @Transactional(readOnly = true)
    fun execute(
        stackIds: List<String>,
        keyword: String?,
    ): GetDependenciesResponse {
        val dependencies =
            if (keyword.isNullOrBlank()) {
                dependencyRepository.findByTechStackPublicIdInOrderByIdAsc(stackIds)
            } else {
                dependencyRepository.findByTechStackPublicIdInAndNameContainingIgnoreCaseOrderByIdAsc(
                    stackIds = stackIds,
                    keyword = keyword.trim(),
                )
            }

        return GetDependenciesResponse(
            dependencies =
                dependencies.map {
                    DependencyResponse.from(it)
                },
        )
    }
}
