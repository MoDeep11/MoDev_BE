package modeep.modev.domain.structure.service

import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.structure.controller.dto.request.GenerateStructureRequest
import modeep.modev.domain.structure.controller.dto.response.GenerateStructureResponse
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GenerateStructureService(
    private val projectRepository: ProjectRepository,
) {
    @Transactional
    fun execute(request: GenerateStructureRequest): GenerateStructureResponse {
        val projectId = request.projectId
        val project =
            projectRepository.findByIdAndDeletedAtIsNull(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        project.status = ProjectStatus.PENDING

        return GenerateStructureResponse(
            projectId = request.projectId,
            status = ProjectStatus.PENDING,
        )
    }
}
