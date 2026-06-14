package modeep.modev.domain.project.controller.dto.response

import java.time.Instant

data class UpdateProjectMetadataResponse(
    val id: String,
    val projectName: String,
    val description: String?,
    val updatedAt: Instant,
)
