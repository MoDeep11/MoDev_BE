package modeep.modev.domain.structure.service

import modeep.modev.domain.structure.controller.dto.response.GetStructureFileResponse
import modeep.modev.domain.structure.entity.vo.StructureFileType
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.GlobalErrorCode
import modeep.modev.global.exception.error.StructureErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetStructureFileService(
    private val structureFileRepository: StructureFileRepository,
) {
    @Transactional(readOnly = true)
    fun execute(
        projectId: UUID,
        path: String,
    ): GetStructureFileResponse {
        val normalizedPath = path.trim()
        validatePath(normalizedPath)

        val file =
            structureFileRepository.findByProjectIdAndPath(projectId, normalizedPath)
                ?.takeIf { it.type == StructureFileType.FILE }
                ?: throw BusinessException(StructureErrorCode.FILE_NOT_FOUND)

        return GetStructureFileResponse(
            filePath = file.path,
            content = file.content.orEmpty(),
            language = file.language,
        )
    }

    // filePath의 값이 유효한지 검사합니다.
    // 안전하지 않은 파일 경로를 차단합니다.
    private fun validatePath(path: String) {
        if (
            path.isBlank() ||
            path.startsWith("/") ||
            path.contains("\\") ||
            path.containsPathTraversal() ||
            WINDOWS_ABSOLUTE_PATH.matches(path)
        ) {
            throw BusinessException(GlobalErrorCode.VALIDATION_ERROR)
        }
    }

    // 파일 탐색 방어
    // "../../etc/something.md" 같은 부모 파일 탐색 차단
    private fun String.containsPathTraversal(): Boolean =
        split("/")
            .any { it == ".." }

    // windows 드라이브 기반 절대 경로 탐지
    // "C:", "D:" 같은 형식 차단
    private companion object {
        val WINDOWS_ABSOLUTE_PATH = Regex("^[A-Za-z]:.*")
    }
}
