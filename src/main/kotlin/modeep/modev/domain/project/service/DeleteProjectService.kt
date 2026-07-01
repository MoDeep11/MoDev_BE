package modeep.modev.domain.project.service

import modeep.modev.domain.project.controller.dto.response.DeleteProjectResponse
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class DeleteProjectService(
    private val projectRepository: ProjectRepository,
) {
    @Transactional
    fun deleteProject(projectId: UUID): DeleteProjectResponse {
        val project =
            projectRepository
                .findByIdAndDeletedAtIsNull(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        val deletedAt = Instant.now()
        val hardDeleteScheduledAt = deletedAt.plus(30, ChronoUnit.DAYS)
        project.delete(
            deletedAt = deletedAt,
        )

        return DeleteProjectResponse(
            projectId = projectId,
            deletedAt = deletedAt,
            hardDeleteScheduledAt = hardDeleteScheduledAt,
        )
    }
}
