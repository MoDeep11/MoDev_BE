package modeep.modev.domain.project.service

import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.project.controller.dto.request.SaveProjectRequest
import modeep.modev.domain.project.controller.dto.request.UpdateProjectMetadataRequest
import modeep.modev.domain.project.controller.dto.response.DeleteProjectResponse
import modeep.modev.domain.project.controller.dto.response.GetProjectDetailResponse
import modeep.modev.domain.project.controller.dto.response.GetProjectsResponse
import modeep.modev.domain.project.controller.dto.response.PaginationResponse
import modeep.modev.domain.project.controller.dto.response.ProjectDependencyResponse
import modeep.modev.domain.project.controller.dto.response.ProjectStackResponse
import modeep.modev.domain.project.controller.dto.response.ProjectSummaryResponse
import modeep.modev.domain.project.controller.dto.response.SaveProjectResponse
import modeep.modev.domain.project.controller.dto.response.UpdateProjectMetadataResponse
import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.entity.ProjectDependency
import modeep.modev.domain.project.entity.ProjectField
import modeep.modev.domain.project.entity.ProjectTechStack
import modeep.modev.domain.project.entity.id.ProjectDependencyId
import modeep.modev.domain.project.entity.id.ProjectFieldId
import modeep.modev.domain.project.entity.id.ProjectTechStackId
import modeep.modev.domain.project.repository.ProjectDependencyRepository
import modeep.modev.domain.project.repository.ProjectFieldRepository
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.project.repository.ProjectTechStackRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val fieldRepository: FieldRepository,
    private val dependencyRepository: DependencyRepository,
    private val techStackRepository: TechStackRepository,
    private val projectFieldRepository: ProjectFieldRepository,
    private val projectTechStackRepository: ProjectTechStackRepository,
    private val projectDependencyRepository: ProjectDependencyRepository,
) {
    @Transactional
    fun saveProject(request: SaveProjectRequest): SaveProjectResponse {
        val projectId = generateProjectId()
        val fields = fieldRepository.findByPublicIdIn(request.fieldIds.distinct())
        val stacks = techStackRepository.findByPublicIdIn(request.stackIds.distinct())
        val dependencies = dependencyRepository.findByPublicIdIn(request.dependencyIds.distinct())
        validateCatalogIds(request, fields.map { it.publicId }, stacks.map { it.publicId }, dependencies.map { it.publicId })

        val project =
            projectRepository.save(
                Project(
                    id = projectId,
                    projectName = request.projectName,
                    description = request.description.normalizeDescription(),
                ),
            )
        projectFieldRepository.saveAll(
            fields.map {
                ProjectField(
                    id = ProjectFieldId(projectId = projectId, fieldId = it.id),
                )
            },
        )
        projectTechStackRepository.saveAll(
            stacks.map {
                ProjectTechStack(
                    id = ProjectTechStackId(projectId = projectId, techStackId = it.id),
                )
            },
        )
        projectDependencyRepository.saveAll(
            dependencies.map {
                ProjectDependency(
                    id = ProjectDependencyId(projectId = projectId, dependencyId = it.id),
                )
            },
        )

        return SaveProjectResponse(
            projectId = project.id,
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
                .findByIdAndDeletedAtIsNull(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        project.updateMetadata(
            projectName = request.projectName,
            description = request.description.normalizeDescription(),
        )

        return UpdateProjectMetadataResponse(
            id = project.id,
            projectName = project.projectName,
            description = project.description,
            updatedAt = project.updatedAt,
        )
    }

    @Transactional
    fun deleteProject(projectId: String): DeleteProjectResponse {
        val project =
            projectRepository
                .findByIdAndDeletedAtIsNull(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        val deletedAt = LocalDateTime.now()
        val hardDeleteScheduledAt = deletedAt.plus(30, ChronoUnit.DAYS)
        project.delete(
            deletedAt = deletedAt,
        )

        return DeleteProjectResponse(
            projectId = project.id,
            deletedAt = deletedAt,
            hardDeleteScheduledAt = hardDeleteScheduledAt,
        )
    }

    @Transactional(readOnly = true)
    fun getProjectDetail(projectId: String): GetProjectDetailResponse {
        val project =
            projectRepository
                .findByIdAndDeletedAtIsNull(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)
        val fields = fieldRepository.findByProjectId(projectId)
        val stacks = techStackRepository.findByProjectId(projectId)
        val dependencies = dependencyRepository.findByProjectId(projectId)

        return GetProjectDetailResponse(
            projectId = project.id,
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
            fileTree = project.structure ?: "[]",
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
        val projectIds = projects.content.map { it.id }
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
                    ProjectSummaryResponse(
                        projectId = project.id,
                        projectName = project.projectName,
                        description = project.description,
                        stacks = stacksByProjectId[project.id].orEmpty(),
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

    private fun generateProjectId(): String = UUID.randomUUID().toString()

    private fun String?.normalizeDescription(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun validateCatalogIds(
        request: SaveProjectRequest,
        fieldIds: List<String>,
        stackIds: List<String>,
        dependencyIds: List<String>,
    ) {
        val missingIds =
            mapOf(
                "fieldIds" to request.fieldIds.toSet() - fieldIds.toSet(),
                "stackIds" to request.stackIds.toSet() - stackIds.toSet(),
                "dependencyIds" to request.dependencyIds.toSet() - dependencyIds.toSet(),
            ).filterValues { it.isNotEmpty() }

        if (missingIds.isNotEmpty()) {
            throw BusinessException(
                errorCode = ProjectErrorCode.INVALID_STACK_COMBINATION,
                details = missingIds,
            )
        }
    }
}
