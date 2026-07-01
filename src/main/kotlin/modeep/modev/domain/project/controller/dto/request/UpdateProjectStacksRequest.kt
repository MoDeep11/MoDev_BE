package modeep.modev.domain.project.controller.dto.request

import jakarta.validation.constraints.Size

data class UpdateProjectStacksRequest(
    @field:Size(min = 1)
    val fieldIds: List<String>,
    @field:Size(min = 1)
    val stackIds: List<String>,
    val dependencyIds: List<String> = emptyList(),
)
