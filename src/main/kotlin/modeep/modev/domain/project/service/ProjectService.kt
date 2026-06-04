package modeep.modev.domain.project.service

import modeep.modev.domain.project.client.ProjectDetailClient
import modeep.modev.domain.project.controller.dto.request.SaveProjectRequest
import modeep.modev.domain.project.controller.dto.request.UpdateProjectMetadataRequest
import modeep.modev.domain.project.controller.dto.response.GetProjectsResponse
import modeep.modev.domain.project.controller.dto.response.PaginationResponse
import modeep.modev.domain.project.controller.dto.response.ProjectSummaryResponse
import modeep.modev.domain.project.controller.dto.response.SaveProjectResponse
import modeep.modev.domain.project.controller.dto.response.UpdateProjectMetadataResponse
import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectDetailClient: ProjectDetailClient,
) {
    @Transactional
    fun saveProject(request: SaveProjectRequest): SaveProjectResponse {
        val projectId = generateProjectId()
        val project =
            projectRepository.save(
                Project(
                    projectId = projectId,
                    generateId = projectId,
                    projectName = request.projectName,
                    description = request.description,
                ),
            )

        return SaveProjectResponse(
            projectId = project.projectId,
            projectName = project.projectName,
            createdAt = project.createdAt,
        )
    }

    @Transactional
    fun updateProjectMetadata(
        projectId: String,
        request: UpdateProjectMetadataRequest,
    ): UpdateProjectMetadataResponse {
        val project =
            projectRepository
                .findById(projectId)
                .orElseThrow { BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND) }

        project.updateMetadata(
            projectName = request.projectName,
            description = request.description,
        )

        return UpdateProjectMetadataResponse(
            projectId = project.projectId,
            projectName = project.projectName,
            description = project.description,
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
                projectRepository.findAll(pageable)
            } else {
                projectRepository.findByProjectNameContainingIgnoreCase(normalizedKeyword, pageable)
            }

        return GetProjectsResponse(
            projects =
                projects.content.map { project ->
                    ProjectSummaryResponse(
                        projectId = project.projectId,
                        projectName = project.projectName,
                        description = project.description,
                        stacks = emptyList(),
                        createdAt = project.createdAt,
                        updatedAt = project.updatedAt,
                        status = "ACTIVE",
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

    private fun generateProjectId(): String = "proj_${UUID.randomUUID().toString().replace("-", "").take(12)}"
}
