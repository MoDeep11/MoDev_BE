package modeep.modev.domain.structure.service

import modeep.modev.domain.structure.ProjectStore
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
    private val projectStore: ProjectStore,
    private val structureFileRepository: StructureFileRepository,
) {
    @Transactional(readOnly = true)
    fun execute(projectId: UUID): GetStructureStatusResponse {
        val project =
            projectStore.get(projectId)
                ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        val status = project.status ?: "PENDING"
        val result =
            if (status == "COMPLETED") {
                StructureResultResponse(
                    fileTree = buildFileTree(structureFileRepository.findAllByProjectIdOrderByPathAsc(projectId)),
                )
            } else {
                null
            }

        return GetStructureStatusResponse(
            projectId = project.id.toString(),
            status = status,
            result = result,
        )
    }

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

        return roots.map { it.toResponse() }
    }

    private fun String.depth(): Int =
        trim('/')
            .takeIf { it.isNotBlank() }
            ?.count { it == '/' }
            ?: 0

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
