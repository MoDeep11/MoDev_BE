package modeep.modev.domain.structure.controller.dto.response

data class GetStructureStatusResponse(
    val projectId: String,
    val status: String,
    val result: StructureResultResponse?,
)
