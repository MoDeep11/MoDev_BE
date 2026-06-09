package modeep.modev.domain.structure.service

import modeep.modev.domain.structure.ProjectStore
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
    private val projectRepository: ProjectStore,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun execute(request: GenerateStructureRequest): GenerateStructureResponse {
        // todo: fetch join 사용하여 쿼리 - n+1 방지
        val project =
            projectRepository.get(request.projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        eventPublisher.publishEvent(
            GenerateStructureEvent(
                projectId = project.id,
                projectName = project.name,
                fields =
                    project.fields.map {
                        FieldInfos.from(it)
                    },
                techStacks =
                    project.techStacks.map {
                        StackInfos.from(it)
                    },
                dependencies =
                    project.dependencies.map {
                        // todo: 당장에 에러는 없으나 n+1 발생 가능
                        DependencyInfos.from(it, it.techStack)
                    },
            ),
        )

        return GenerateStructureResponse(
            projectId = project.id,
            status = "PENDING",
        )
    }
}
