package modeep.modev.domain.structure.controller.dto.response

import java.util.UUID

data class GetStructureStatusResponse(
    val projectId: UUID,
    val status: String,
    val result: StructureResultResponse?,
)

data class StructureResultResponse(
    val fileTree: List<FileTreeNodeResponse>,
)

data class FileTreeNodeResponse(
    val name: String,
    val type: String,
    val children: List<FileTreeNodeResponse> = emptyList(),
)
