package modeep.modev.domain.structure.worker

import modeep.modev.domain.catalog.repository.DependencyRepository
import modeep.modev.domain.catalog.repository.FieldRepository
import modeep.modev.domain.catalog.repository.TechStackRepository
import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.structure.controller.dto.response.FileCreatedStreamResponse
import modeep.modev.domain.structure.entity.StructureFile
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.domain.structure.worker.event.DependencyInfos
import modeep.modev.domain.structure.worker.event.FieldInfos
import modeep.modev.domain.structure.worker.event.GenerateStructureEvent
import modeep.modev.domain.structure.worker.event.StackInfos
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import modeep.modev.global.util.LanguageDetector.detect
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class StructureStatusService(
    private val projectRepository: ProjectRepository,
    private val structureFileRepository: StructureFileRepository,
    private val fieldRepository: FieldRepository,
    private val techStackRepository: TechStackRepository,
    private val dependencyRepository: DependencyRepository,
    private val eventPublisher: ApplicationEventPublisher,
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
    fun publishGenerating(projectId: UUID) {
        val project =
            projectRepository
                .findByIdAndDeletedAtIsNullForUpdate(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        // 상태가 PENDING(생성 요청을 한 상태인지)인지 검증
        if (project.status != ProjectStatus.PENDING) {
            throw BusinessException(ProjectErrorCode.PROJECT_STRUCTURE_NOT_PENDING)
        }

        // GENERATING 상태로 변경하여 같은 요청이 2번 들어와 이벤트를 2번 발행하는 것 방지
        project.status = ProjectStatus.GENERATING

        val fields = fieldRepository.findByProjectId(projectId)
        val techStacks = techStackRepository.findByProjectId(projectId)
        val dependencies = dependencyRepository.findByProjectId(projectId)

        eventPublisher.publishEvent(
            GenerateStructureEvent(
                projectId = projectId,
                projectName = project.projectName,
                fields =
                    fields.map {
                        FieldInfos.from(it)
                    },
                techStacks =
                    techStacks.map {
                        StackInfos.from(it)
                    },
                dependencies =
                    dependencies.map {
                        DependencyInfos.from(it, it.techStack)
                    },
            ),
        )
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
