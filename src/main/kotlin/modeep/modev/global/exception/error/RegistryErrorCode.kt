package modeep.modev.global.exception.error

import modeep.modev.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class RegistryErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "REGI-001", "레지스트리 동기화 대상 catalog 항목을 찾을 수 없습니다."),
    METADATA_REQUIRED(HttpStatus.BAD_REQUEST, "REGI-002", "레지스트리 조회 메타데이터가 설정되어 있지 않습니다."),
    INVALID_IDENTIFIER(HttpStatus.BAD_REQUEST, "REGI-003", "레지스트리 식별자 형식이 올바르지 않습니다."),
    VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, "REGI-004", "레지스트리에서 동기화할 버전을 찾을 수 없습니다."),
    CLIENT_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "REGI-005", "레지스트리 클라이언트가 설정되어 있지 않습니다."),
    EMPTY_RESPONSE(HttpStatus.BAD_GATEWAY, "REGI-006", "레지스트리 응답이 비어 있습니다."),
    REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "REGI-007", "레지스트리 API 호출에 실패했습니다."),
    RESPONSE_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "REGI-008", "레지스트리 응답 파싱에 실패했습니다."),

    FIELD_NOT_FOUND_IN_TECH_STACK(HttpStatus.NOT_FOUND, "REGI-009", "기술 스택 초기 데이터(Seed)에서 해당 필드를 찾을 수 없습니다"),
    TECH_STACK_NOT_FOUND_IN_DEPENDENCY(HttpStatus.NOT_FOUND, "REGI-010", "의존성 초기 데이터(Seed)에서 해당 기술 스택을 찾을 수 없습니다"),
}
