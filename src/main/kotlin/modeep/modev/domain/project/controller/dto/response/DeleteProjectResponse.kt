package modeep.modev.domain.project.controller.dto.response

import java.time.Instant

data class DeleteProjectResponse(
    val projectId: String,
    val deletedAt: Instant,
    val hardDeleteScheduledAt: Instant,
)
