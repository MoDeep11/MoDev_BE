package modeep.modev.domain.project.controller.dto.response

import modeep.modev.domain.project.entity.ProjectStatus
import java.util.UUID

data class UpdateProjectStacksResponse(
    val projectId: UUID,
    val status: ProjectStatus,
)
