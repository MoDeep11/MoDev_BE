package modeep.modev.domain.structure.controller.dto.response

import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.structure.controller.dto.response.vo.StructureFileType

data class ConnectedStreamResponse(
    val projectId: String,
    val message: String,
)

data class StatusStreamResponse(
    val projectId: String,
    val status: ProjectStatus,
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
