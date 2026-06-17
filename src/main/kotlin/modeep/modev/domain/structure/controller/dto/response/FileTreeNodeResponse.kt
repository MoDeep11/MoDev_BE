package modeep.modev.domain.structure.controller.dto.response

data class FileTreeNodeResponse(
    val name: String,
    val type: String,
    val children: List<FileTreeNodeResponse> = emptyList(),
)
