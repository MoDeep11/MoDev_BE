package modeep.modev.domain.structure.service

import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.structure.controller.dto.response.FileTreeNodeResponse
import modeep.modev.domain.structure.controller.dto.response.GetStructureStatusResponse
import modeep.modev.domain.structure.controller.dto.response.StructureResultResponse
import modeep.modev.domain.structure.entity.StructureFile
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetStructureStatusService(
    private val projectRepository: ProjectRepository,
    private val structureFileRepository: StructureFileRepository,
) {
    @Transactional(readOnly = true)
    fun execute(projectId: UUID): GetStructureStatusResponse {
        val project =
            projectRepository.findByIdAndDeletedAtIsNull(projectId.toString())
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        val status = project.status
        val result =
            if (status == ProjectStatus.COMPLETED) {
                StructureResultResponse(
                    fileTree = buildFileTree(structureFileRepository.findAllByProjectIdOrderByPathAsc(projectId)),
                )
            } else {
                null
            }

        return GetStructureStatusResponse(
            projectId = project.id,
            status = status.name,
            result = result,
        )
    }

    // 파일 구조 생성, StructureFile 정보 기반
    private fun buildFileTree(files: List<StructureFile>): List<FileTreeNodeResponse> {
        val roots = mutableListOf<TreeNode>()
        val nodesByPath = mutableMapOf<String, TreeNode>()

        files.sortedWith(compareBy<StructureFile> { it.path.depth() }.thenBy { it.path })
            .forEach { file ->
                val normalizedPath = file.path.trim('/')
                if (normalizedPath.isBlank()) {
                    return@forEach
                }

                val segments = normalizedPath.split("/")
                segments.indices.forEach { index ->
                    val currentPath = segments.take(index + 1).joinToString("/")
                    val isLeaf = index == segments.lastIndex
                    val nodeType =
                        if (isLeaf) {
                            file.type
                        } else {
                            StructureFileType.DIRECTORY
                        }
                    val node =
                        nodesByPath.getOrPut(currentPath) {
                            TreeNode(
                                name = segments[index],
                                type = nodeType,
                            )
                        }.also { it.type = nodeType }

                    if (index == 0) {
                        if (roots.none { it.name == node.name }) {
                            roots += node
                        }
                    } else {
                        val parentPath = segments.take(index).joinToString("/")
                        val parent = nodesByPath.getValue(parentPath)
                        if (parent.children.none { it.name == node.name }) {
                            parent.children += node
                        }
                    }
                }
            }

        // 한 번 더 정렬하여 응답
        return roots
            .sortedWith(compareBy<TreeNode> { it.type != StructureFileType.DIRECTORY }.thenBy { it.name })
            .map { it.toResponse() }
    }

    // 레벨 반환
    private fun String.depth(): Int =
        trim('/')
            .takeIf { it.isNotBlank() }
            ?.count { it == '/' }
            ?: 0

    // 구조 노드로 사용되는 내부 데이터 클래스
    private data class TreeNode(
        val name: String,
        var type: StructureFileType,
        val children: MutableList<TreeNode> = mutableListOf(),
    ) {
        fun toResponse(): FileTreeNodeResponse =
            FileTreeNodeResponse(
                name = name,
                type = type.name,
                children =
                    children
                        .sortedWith(compareBy<TreeNode> { it.type != StructureFileType.DIRECTORY }.thenBy { it.name })
                        .map { it.toResponse() },
            )
    }
}
