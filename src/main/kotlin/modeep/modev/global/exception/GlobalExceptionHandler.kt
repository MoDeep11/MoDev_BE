package modeep.modev.global.exception

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import modeep.modev.global.exception.error.GlobalErrorCode
import modeep.modev.global.response.ApiResponse
import modeep.modev.global.response.ErrorLog
import modeep.modev.global.response.ErrorResponse
import modeep.modev.global.response.LogEvent
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val objectMapper: ObjectMapper,
) {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler(BusinessException::class)
    fun handlerBusinessException(
        e: BusinessException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        val traceId = MDC.get("traceId")
        val userId = MDC.get("userId")
        val clientIp = MDC.get("clientIp")

        log.error(e) {
            objectMapper.writeValueAsString(
                ErrorLog(
                    event = LogEvent.BUSINESS_ERROR.name,
                    traceId = traceId,
                    userId = userId,
                    clientIp = clientIp,
                    method = request.method,
                    path = request.requestURI,
                    query = request.queryString,
                    exception = e.javaClass.simpleName,
                    message = e.message,
                ),
            )
        }

        val errorCode: ErrorCode = e.errorCode

        return ResponseEntity
            .status(errorCode.status.value())
            .body(
                ApiResponse(
                    success = false,
                    data = null,
                    error =
                        ErrorResponse(
                            code = errorCode,
                            message = errorCode.message,
                        ),
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        val traceId = MDC.get("traceId")
        val userId = MDC.get("userId")
        val clientIp = MDC.get("clientIp")

        log.error(e) {
            objectMapper.writeValueAsString(
                ErrorLog(
                    event = LogEvent.UNEXPECTED_ERROR.name,
                    traceId = traceId,
                    userId = userId,
                    clientIp = clientIp,
                    method = request.method,
                    path = request.requestURI,
                    query = request.queryString,
                    exception = e.javaClass.simpleName,
                    message = e.message,
                ),
            )
        }

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .body(
                ApiResponse(
                    success = false,
                    data = null,
                    error =
                        ErrorResponse(
                            code = GlobalErrorCode.INTERNAL_ERROR,
                            message = GlobalErrorCode.INTERNAL_ERROR.message,
                        ),
                ),
            )
    }
}
