package modeep.modev.domain.project.controller.dto.response

import java.time.Instant

data class SaveProjectResponse(
    val projectId: String,
    val projectName: String,
    val createdAt: Instant,
)
