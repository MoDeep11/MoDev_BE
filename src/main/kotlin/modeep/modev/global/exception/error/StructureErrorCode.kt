package modeep.modev.global.exception.error

import modeep.modev.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class StructureErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STRU-001", "AI 구조 생성에 실패했습니다. 다시 시도해 주세요."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "STRU-002", "파일을 찾을 수 없습니다."),
}
