package modeep.modev.domain.project.controller.dto.response

import java.time.Instant
import java.util.UUID

data class UpdateProjectMetadataResponse(
    val projectId: UUID,
    val projectName: String,
    val description: String?,
    val updatedAt: Instant,
)
