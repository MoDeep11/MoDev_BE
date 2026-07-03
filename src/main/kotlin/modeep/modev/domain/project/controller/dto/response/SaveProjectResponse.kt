package modeep.modev.domain.project.controller.dto.response

import java.time.Instant
import java.util.UUID

data class SaveProjectResponse(
    val projectId: UUID,
    val projectName: String,
    val createdAt: Instant,
)
