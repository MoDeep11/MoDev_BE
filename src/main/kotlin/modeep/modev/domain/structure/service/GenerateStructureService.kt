package modeep.modev.domain.structure.service

import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.structure.controller.dto.request.GenerateStructureRequest
import modeep.modev.domain.structure.controller.dto.response.GenerateStructureResponse
import modeep.modev.domain.structure.worker.event.DependencyInfos
import modeep.modev.domain.structure.worker.event.FieldInfos
import modeep.modev.domain.structure.worker.event.GenerateStructureEvent
import modeep.modev.domain.structure.worker.event.StackInfos
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GenerateStructureService(
    private val projectRepository: ProjectRepository,
    private val fieldRepository: FieldRepository,
    private val techStackRepository: TechStackRepository,
    private val dependencyRepository: DependencyRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun execute(request: GenerateStructureRequest): GenerateStructureResponse {
        val projectId = request.projectId.toString()
        val project =
            projectRepository.findByIdAndDeletedAtIsNull(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)
        val fields = fieldRepository.findByProjectId(projectId)
        val techStacks = techStackRepository.findByProjectId(projectId)
        val dependencies = dependencyRepository.findByProjectId(projectId)

        project.status = ProjectStatus.PENDING

        eventPublisher.publishEvent(
            GenerateStructureEvent(
                projectId = request.projectId,
                projectName = project.projectName,
                fields =
                    fields.map {
                        FieldInfos.from(it)
                    },
                techStacks =
                    techStacks.map {
                        StackInfos.from(it)
                    },
                dependencies =
                    dependencies.map {
                        // todo: 당장에 에러는 없으나 n+1 발생 가능
                        DependencyInfos.from(it, it.techStack)
                    },
            ),
        )

        return GenerateStructureResponse(
            projectId = request.projectId,
            status = "PENDING",
        )
    }
}
