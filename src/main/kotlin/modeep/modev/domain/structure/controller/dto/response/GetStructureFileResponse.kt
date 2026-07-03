package modeep.modev.domain.structure.controller.dto.response

data class GetStructureFileResponse(
    val filePath: String,
    val content: String,
    val language: String,
)
