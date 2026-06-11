package modeep.modev.domain.project.controller.dto.response

import java.time.LocalDateTime

data class SaveProjectResponse(
    val projectId: String,
    val projectName: String,
    val createdAt: LocalDateTime,
)
