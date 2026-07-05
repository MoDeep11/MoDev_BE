package modeep.modev.domain.project.service

import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.project.controller.dto.request.SaveProjectRequest
import modeep.modev.domain.project.controller.dto.response.SaveProjectResponse
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
import modeep.modev.global.exception.error.GlobalErrorCode
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostProjectService(
    private val projectRepository: ProjectRepository,
    private val fieldRepository: FieldRepository,
    private val dependencyRepository: DependencyRepository,
    private val techStackRepository: TechStackRepository,
    private val projectFieldRepository: ProjectFieldRepository,
    private val projectTechStackRepository: ProjectTechStackRepository,
    private val projectDependencyRepository: ProjectDependencyRepository,
) {
    @Transactional
    fun saveProject(
        request: SaveProjectRequest,
        userId: Long?,
    ): SaveProjectResponse {
        val fields = fieldRepository.findByPublicIdIn(request.fieldIds.distinct())
        val stacks = techStackRepository.findByPublicIdIn(request.stackIds.distinct())
        val dependencies = dependencyRepository.findByPublicIdIn(request.dependencyIds.distinct())
        validateCatalogIds(request, fields.map { it.publicId }, stacks.map { it.publicId }, dependencies.map { it.publicId })
        validateStackCombination(fields.map { it.publicId }.toSet(), stacks.map { it.publicId }.toSet())

        val project =
            projectRepository.save(
                Project(
                    projectName = request.projectName,
                    description = request.description.normalizeDescription(),
                    userId = userId,
                ),
            )
        val projectId = requireNotNull(project.id)

        projectFieldRepository.saveAll(
            fields.map {
                ProjectField(
                    id = ProjectFieldId(projectId = projectId, fieldId = requireNotNull(it.id)),
                )
            },
        )
        projectTechStackRepository.saveAll(
            stacks.map {
                ProjectTechStack(
                    id = ProjectTechStackId(projectId = projectId, techStackId = requireNotNull(it.id)),
                    version = it.version,
                )
            },
        )
        projectDependencyRepository.saveAll(
            dependencies.map {
                ProjectDependency(
                    id = ProjectDependencyId(projectId = projectId, dependencyId = requireNotNull(it.id)),
                    version = it.version,
                )
            },
        )

        return SaveProjectResponse(
            projectId = projectId,
            projectName = project.projectName,
            createdAt = project.createdAt,
        )
    }

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
                errorCode = GlobalErrorCode.VALIDATION_ERROR,
                details = missingIds,
            )
        }
    }

    private fun validateStackCombination(
        fieldIds: Set<String>,
        stackIds: Set<String>,
    ) {
        val mappedStackIds =
            techStackRepository
                .findStacksByFieldPublicIds(fieldIds)
                .mapTo(mutableSetOf()) { it.publicId }
        val invalidStackIds = stackIds - mappedStackIds

        if (invalidStackIds.isNotEmpty()) {
            throw BusinessException(
                errorCode = ProjectErrorCode.INVALID_STACK_COMBINATION,
                details = mapOf("stackIds" to invalidStackIds),
            )
        }
    }
}
