package modeep.modev.domain.project.controller.dto.response

import java.time.Instant
import java.util.UUID

data class DeleteProjectResponse(
    val projectId: UUID,
    val deletedAt: Instant,
    val hardDeleteScheduledAt: Instant,
)
