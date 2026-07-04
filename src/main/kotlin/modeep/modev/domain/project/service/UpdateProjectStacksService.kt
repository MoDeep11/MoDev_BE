package modeep.modev.domain.project.service

import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.project.controller.dto.request.UpdateProjectStacksRequest
import modeep.modev.domain.project.controller.dto.response.UpdateProjectStacksResponse
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
import modeep.modev.domain.structure.controller.dto.request.GenerateStructureRequest
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.domain.structure.service.GenerateStructureService
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.GlobalErrorCode
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateProjectStacksService(
    private val projectRepository: ProjectRepository,
    private val fieldRepository: FieldRepository,
    private val techStackRepository: TechStackRepository,
    private val dependencyRepository: DependencyRepository,
    private val projectFieldRepository: ProjectFieldRepository,
    private val projectTechStackRepository: ProjectTechStackRepository,
    private val projectDependencyRepository: ProjectDependencyRepository,
    private val structureFileRepository: StructureFileRepository,
    private val generateStructureService: GenerateStructureService,
) {
    @Transactional
    fun execute(
        projectId: UUID,
        request: UpdateProjectStacksRequest,
    ): UpdateProjectStacksResponse {
        projectRepository.findByIdAndDeletedAtIsNull(projectId)
            ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        val fieldIds = request.fieldIds.distinct()
        val stackIds = request.stackIds.distinct()
        val dependencyIds = request.dependencyIds.distinct()
        val fields = fieldRepository.findByPublicIdIn(fieldIds)
        val stacks = techStackRepository.findByPublicIdIn(stackIds)
        val dependencies = dependencyRepository.findByPublicIdIn(dependencyIds)

        validateCatalogIds(request, fields.map { it.publicId }, stacks.map { it.publicId }, dependencies.map { it.publicId })
        validateStackCombination(fields.mapTo(mutableSetOf()) { it.publicId }, stacks.mapTo(mutableSetOf()) { it.publicId })
        validateDependencyCombination(stacks.mapTo(mutableSetOf()) { it.publicId }, dependencies.mapTo(mutableSetOf()) { it.publicId })

        projectFieldRepository.deleteAllByIdProjectId(projectId)
        projectTechStackRepository.deleteAllByIdProjectId(projectId)
        projectDependencyRepository.deleteAllByIdProjectId(projectId)

        projectFieldRepository.saveAllAndFlush(
            fields.map {
                ProjectField(
                    id = ProjectFieldId(projectId = projectId, fieldId = requireNotNull(it.id)),
                )
            },
        )
        projectTechStackRepository.saveAllAndFlush(
            stacks.map {
                ProjectTechStack(
                    id = ProjectTechStackId(projectId = projectId, techStackId = requireNotNull(it.id)),
                    version = it.version,
                )
            },
        )
        projectDependencyRepository.saveAllAndFlush(
            dependencies.map {
                ProjectDependency(
                    id = ProjectDependencyId(projectId = projectId, dependencyId = requireNotNull(it.id)),
                    version = it.version,
                )
            },
        )

        structureFileRepository.deleteAllByProjectId(projectId)
        val generation = generateStructureService.execute(GenerateStructureRequest(projectId))

        return UpdateProjectStacksResponse(
            projectId = generation.projectId,
            status = generation.status,
        )
    }

    private fun validateCatalogIds(
        request: UpdateProjectStacksRequest,
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
        val supportedStackIds =
            techStackRepository
                .findStacksByFieldPublicIds(fieldIds)
                .mapTo(mutableSetOf()) { it.publicId }
        val invalidStackIds = stackIds - supportedStackIds

        if (invalidStackIds.isNotEmpty()) {
            throw BusinessException(
                errorCode = ProjectErrorCode.INVALID_STACK_COMBINATION,
                details = mapOf("stackIds" to invalidStackIds),
            )
        }
    }

    private fun validateDependencyCombination(
        stackIds: Set<String>,
        dependencyIds: Set<String>,
    ) {
        val supportedDependencyIds =
            dependencyRepository
                .findByTechStackPublicIdInOrderByIdAsc(stackIds)
                .mapTo(mutableSetOf()) { it.publicId }
        val invalidDependencyIds = dependencyIds - supportedDependencyIds

        if (invalidDependencyIds.isNotEmpty()) {
            throw BusinessException(
                errorCode = ProjectErrorCode.INVALID_STACK_COMBINATION,
                details = mapOf("dependencyIds" to invalidDependencyIds),
            )
        }
    }
}
