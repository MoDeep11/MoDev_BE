package modeep.modev.domain.project.controller.dto.response

import java.util.UUID

data class UpdateProjectStacksResponse(
    val projectId: UUID,
    val status: String,
)
