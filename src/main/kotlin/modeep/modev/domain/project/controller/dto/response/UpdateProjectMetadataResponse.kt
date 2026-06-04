package modeep.modev.domain.project.controller.dto.response

import java.time.Instant

data class UpdateProjectMetadataResponse(
    val projectId: String,
    val projectName: String,
    val description: String?,
    val updatedAt: Instant,
)
