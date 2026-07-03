package modeep.modev.domain.structure.worker

import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.structure.controller.dto.response.FileCreatedStreamResponse
import modeep.modev.domain.structure.entity.StructureFile
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import modeep.modev.global.util.LanguageDetector.detect
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class StructureStatusService(
    private val projectRepository: ProjectRepository,
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

    @Transactional
    fun markGenerating(projectId: UUID) {
        updateProject(projectId) {
            status = ProjectStatus.GENERATING
        }
    }

    @Transactional
    fun markCompleted(
        projectId: UUID,
        result: String,
    ) {
        updateProject(projectId) {
            status = ProjectStatus.COMPLETED
            structure = result
        }
    }

    @Transactional
    fun markFailed(projectId: UUID) {
        updateProject(projectId) {
            status = ProjectStatus.FAILED
        }
    }

    private fun updateProject(
        projectId: UUID,
        update: Project.() -> Unit,
    ) {
        val project =
            projectRepository
                .findByIdAndDeletedAtIsNull(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        project.apply(update)
    }
}
