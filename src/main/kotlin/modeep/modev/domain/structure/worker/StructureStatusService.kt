package modeep.modev.domain.structure.worker

import modeep.modev.domain.structure.ProjectStore
import modeep.modev.domain.structure.StructureFile
import modeep.modev.domain.structure.controller.dto.response.FileCreatedStreamResponse
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
        repository.updateStatus(projectId, "IN_PROGRESS")
    }

    @Transactional
    fun saveFileCreated(
        projectId: UUID,
        file: FileCreatedStreamResponse,
    ) {
        repository.saveStructureFile(
            projectId,
            StructureFile(
                type = file.type.name,
                path = file.path,
                depth = file.depth,
                content = file.content,
            ),
        )
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
