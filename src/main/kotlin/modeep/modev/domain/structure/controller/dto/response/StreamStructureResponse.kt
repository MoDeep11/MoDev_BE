package modeep.modev.domain.structure.controller.dto.response

import modeep.modev.domain.structure.controller.dto.response.vo.StructureFileType
import modeep.modev.domain.structure.controller.dto.response.vo.StructureProgressStep

data class ConnectedStreamResponse(
    val projectId: String,
    val message: String,
)

data class ProgressStreamResponse(
    val step: StructureProgressStep,
    val message: String,
)

data class FileCreatedStreamResponse(
    val type: StructureFileType,
    val path: String,
    val depth: Int,
    val content: String? = null,
)

data class CompleteStreamResponse(
    val projectId: String,
    val totalFiles: Int,
    val totalDirectories: Int,
    val message: String,
)

data class ErrorStreamResponse(
    val code: String,
    val message: String,
)
