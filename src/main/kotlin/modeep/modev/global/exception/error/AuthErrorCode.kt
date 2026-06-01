package modeep.modev.global.exception.error

import modeep.modev.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "AUTH-001", "비밀번호는 최소 8자, 영문+숫자+특수문자 조합이어야 합니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-002", "비밀번호가 일치하지 않습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH-003", "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-004", "이메일 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH-005", "이메일 인증이 필요합니다."),
    ACCOUNT_LOCKED(HttpStatus.LOCKED, "AUTH-006", "로그인 시도가 너무 많습니다. 30분 후 다시 시도해주세요."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-007", "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH-008", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "AUTH-009", "보안 이상이 감지되어 전체 세션이 만료되었습니다. 다시 로그인해주세요."),
    VERIFY_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH-010", "유효하지 않은 인증 링크입니다."),
    ALREADY_VERIFIED(HttpStatus.CONFLICT, "AUTH-011", "이미 인증이 완료된 계정입니다."),
    VERIFY_TOKEN_EXPIRED(HttpStatus.GONE, "AUTH-012", "인증 링크가 만료되었습니다. 인증 이메일을 재발송해주세요."),
    RESEND_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "AUTH-013", "잠시 후 다시 시도해주세요. (1분에 1회 제한)"),
}
