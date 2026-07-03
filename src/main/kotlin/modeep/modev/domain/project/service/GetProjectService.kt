package modeep.modev.domain.project.service

import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.project.controller.dto.response.GetProjectDetailResponse
import modeep.modev.domain.project.controller.dto.response.GetProjectsResponse
import modeep.modev.domain.project.controller.dto.response.PaginationResponse
import modeep.modev.domain.project.controller.dto.response.ProjectDependencyResponse
import modeep.modev.domain.project.controller.dto.response.ProjectStackResponse
import modeep.modev.domain.project.controller.dto.response.ProjectSummaryResponse
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.structure.service.GetStructureStatusService
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetProjectService(
    private val projectRepository: ProjectRepository,
    private val fieldRepository: FieldRepository,
    private val dependencyRepository: DependencyRepository,
    private val techStackRepository: TechStackRepository,
    private val getStructureStatusService: GetStructureStatusService,
) {
    @Transactional(readOnly = true)
    fun getProjectDetail(projectId: UUID): GetProjectDetailResponse {
        val project =
            projectRepository
                .findByIdAndDeletedAtIsNull(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)
        val fields = fieldRepository.findByProjectId(projectId)
        val stacks = techStackRepository.findByProjectId(projectId)
        val dependencies = dependencyRepository.findByProjectId(projectId)

        return GetProjectDetailResponse(
            projectId = projectId,
            projectName = project.projectName,
            description = project.description,
            fields = fields.map { it.name },
            stacks =
                stacks.map {
                    ProjectStackResponse(
                        stackId = it.publicId,
                        name = it.name,
                        category = it.category.name,
                    )
                },
            dependencies =
                dependencies.map {
                    ProjectDependencyResponse(
                        dependencyId = it.publicId,
                        name = it.name,
                        version = it.version,
                        stackId = it.techStack.publicId,
                    )
                },
            fileTree = getStructureStatusService.execute(projectId),
            createdAt = project.createdAt,
            updatedAt = project.updatedAt,
        )
    }

    @Transactional(readOnly = true)
    fun getProjects(
        page: Int,
        size: Int,
        keyword: String?,
    ): GetProjectsResponse {
        val currentPage = page.coerceAtLeast(1)
        val pageSize = size.coerceAtLeast(1).coerceAtMost(100)
        val pageable = PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val normalizedKeyword = keyword?.trim().orEmpty()

        val projects =
            if (normalizedKeyword.isBlank()) {
                projectRepository.findByDeletedAtIsNull(pageable)
            } else {
                projectRepository.findByProjectNameContainingIgnoreCaseAndDeletedAtIsNull(normalizedKeyword, pageable)
            }
        val projectIds = projects.content.map { requireNotNull(it.id) }
        val stacksByProjectId =
            if (projectIds.isEmpty()) {
                emptyMap()
            } else {
                techStackRepository
                    .findByProjectIdIn(projectIds)
                    .groupBy({ it.projectId }, { it.name })
            }

        return GetProjectsResponse(
            projects =
                projects.content.map { project ->
                    val id = requireNotNull(project.id)
                    ProjectSummaryResponse(
                        projectId = id,
                        projectName = project.projectName,
                        description = project.description,
                        stacks = stacksByProjectId[id].orEmpty(),
                        createdAt = project.createdAt,
                        updatedAt = project.updatedAt,
                        status = project.status.name,
                    )
                },
            pagination =
                PaginationResponse(
                    currentPage = currentPage,
                    totalPages = projects.totalPages,
                    totalCount = projects.totalElements,
                    size = pageSize,
                ),
        )
    }
}
