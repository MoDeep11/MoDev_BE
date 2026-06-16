package modeep.modev.domain.project.service

import modeep.modev.domain.project.controller.dto.request.UpdateProjectMetadataRequest
import modeep.modev.domain.project.controller.dto.response.UpdateProjectMetadataResponse
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PatchProjectService(
    private val projectRepository: ProjectRepository,
) {
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

    private fun String?.normalizeDescription(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
