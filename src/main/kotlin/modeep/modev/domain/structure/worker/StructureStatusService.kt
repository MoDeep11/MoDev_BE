package modeep.modev.domain.structure.worker

import modeep.modev.domain.structure.ProjectStore
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class StructureStatusService(
    // TODO: ProjectStore 제거 필요
    private val repository: ProjectStore,
) {
    @Transactional
    fun markGenerating(projectId: UUID) {
        repository.updateStatus(projectId, "GENERATING")
    }

    @Transactional
    fun markCompleted(
        projectId: UUID,
        result: String,
    ) {
        repository.updateStatus(projectId, "COMPLETED")
        repository.updateStructure(projectId, result)
    }

    @Transactional
    fun markFailed(projectId: UUID) {
        repository.updateStatus(projectId, "FAILED")
    }
}
