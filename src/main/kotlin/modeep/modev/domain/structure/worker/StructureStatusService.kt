package modeep.modev.domain.structure.worker

import modeep.modev.domain.structure.ProjectStore
import modeep.modev.domain.structure.controller.dto.response.FileCreatedStreamResponse
import modeep.modev.domain.structure.entity.StructureFile
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.util.LanguageDetector.detect
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class StructureStatusService(
    // TODO: ProjectStore 제거 필요
    private val projectStore: ProjectStore,
    private val structureFileRepository: StructureFileRepository,
) {
    // 비동기 처리 시 Transaction 풀이 너무 오랫동안 열려있는 것을 방지하기 위해 다른 컴포넌트에서 트랜잭션 처리
    @Transactional
    fun saveFileCreated(
        projectId: UUID,
        file: FileCreatedStreamResponse,
    ) {
        val structureFile =
            structureFileRepository.findByProjectIdAndPath(projectId, file.path)
                ?.apply {
                    update(
                        type = StructureFileType.valueOf(file.type.name),
                        depth = file.depth,
                        content = file.content,
                        language = detect(file.path),
                    )
                }
                ?: StructureFile(
                    projectId = projectId,
                    type = StructureFileType.valueOf(file.type.name),
                    path = file.path,
                    depth = file.depth,
                    content = file.content,
                    language = detect(file.path),
                )

        structureFileRepository.save(structureFile)
    }

    // todo: status 이넘 처리 및 store 삭제 필요
    @Transactional
    fun markGenerating(projectId: UUID) {
        projectStore.updateStatus(projectId, "GENERATING")
    }

    @Transactional
    fun markCompleted(
        projectId: UUID,
        result: String,
    ) {
        projectStore.updateStatus(projectId, "COMPLETED")
        projectStore.updateStructure(projectId, result)
    }

    @Transactional
    fun markFailed(projectId: UUID) {
        projectStore.updateStatus(projectId, "FAILED")
    }
}
