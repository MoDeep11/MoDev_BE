package modeep.modev.global.exception.error

import modeep.modev.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class ProjectErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJ-001", "생성 결과를 찾을 수 없거나 만료되었습니다."),
    INVALID_STACK_COMBINATION(HttpStatus.BAD_REQUEST, "PROJ-002", "지원하지 않는 스택 조합입니다."),
    PROJECT_NOT_COMPLETED(HttpStatus.CONFLICT, "PROJ-003", "프로젝트 구조 생성이 완료되지 않았습니다."),
    PROJECT_STRUCTURE_NOT_PENDING(HttpStatus.CONFLICT, "PROJ-004", "생성 대기 중인 상태가 아닙니다. 프로젝트 구조를 생성할 수 없습니다."),
}
