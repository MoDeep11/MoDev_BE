package modeep.modev.domain.structure.controller.dto.response

import java.util.UUID

data class GenerateStructureResponse(
    val projectId: UUID,
    // TODO: 나중에 enum으로 변경
    val status: String,
)
