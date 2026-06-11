package modeep.modev.domain.project.controller.dto.response

import java.time.LocalDateTime

data class DeleteProjectResponse(
    val projectId: String,
    val deletedAt: LocalDateTime,
    val hardDeleteScheduledAt: LocalDateTime,
)
