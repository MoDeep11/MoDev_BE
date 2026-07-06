package modeep.modev.domain.structure.worker.result

import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.structure.worker.event.GenerateStructureEvent

data class StartGenerationResult(
    val status: ProjectStatus,
    val event: GenerateStructureEvent? = null,
)
