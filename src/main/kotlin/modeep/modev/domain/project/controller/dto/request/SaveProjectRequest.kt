package modeep.modev.domain.project.controller.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SaveProjectRequest(
    @field:NotBlank
    @field:Size(max = 50)
    val projectName: String,
    @field:Size(max = 500)
    val description: String? = null,
    @field:Size(min = 1)
    val fieldIds: List<String>,
    @field:Size(min = 1)
    val stackIds: List<String>,
    val dependencyIds: List<String> = emptyList(),
)
