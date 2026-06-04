package modeep.modev.domain.structure.controller.dto.request

data class GenerateStructureRequest(
    val projectName: String,
    val fieldIds: List<String>,
    val stackIds: List<String>,
    val dependencyIds: List<String> = emptyList(),
)
