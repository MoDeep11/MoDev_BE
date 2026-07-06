package modeep.modev.domain.structure.controller.dto.response

import modeep.modev.domain.project.entity.ProjectStatus
import java.util.UUID

data class GenerateStructureResponse(
    val projectId: UUID,
    val status: ProjectStatus,
)
