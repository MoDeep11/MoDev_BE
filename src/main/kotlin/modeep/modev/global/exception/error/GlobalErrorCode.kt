package modeep.modev.global.exception.error

import modeep.modev.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class GlobalErrorCode(
    override val status: HttpStatus,
    override val code: String,
    override val message: String,
) : ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "GLOBAL-001", "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "GLOBAL-002", "Forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "GLOBAL-003", "Not Found"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "GLOBAL-004", "Validation Error"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL-005", "Internal Server Error"),
}
