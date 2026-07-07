package modeep.modev.global.exception

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import modeep.modev.global.exception.error.GlobalErrorCode
import modeep.modev.global.response.ApiResponse
import modeep.modev.global.response.ErrorLog
import modeep.modev.global.response.ErrorResponse
import modeep.modev.global.response.LogEvent
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException

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
        logException(LogEvent.BUSINESS_ERROR, e, request)

        val errorCode: ErrorCode = e.errorCode

        return errorResponse(errorCode)
    }

    // 검증 실패 등 질못된 요청
    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        BindException::class,
        HandlerMethodValidationException::class,
        ConstraintViolationException::class,
        MissingServletRequestParameterException::class,
        MethodArgumentTypeMismatchException::class,
        HttpMessageNotReadableException::class,
        ServletRequestBindingException::class,
    )
    fun handleBadRequest(
        e: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        logException(LogEvent.BUSINESS_ERROR, e, request)

        return errorResponse(GlobalErrorCode.VALIDATION_ERROR)
    }

    // 리소스를 찾을 수 없음
    @ExceptionHandler(
        NoHandlerFoundException::class,
        NoResourceFoundException::class,
    )
    fun handleNotFound(
        e: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        logException(LogEvent.BUSINESS_ERROR, e, request)

        return errorResponse(GlobalErrorCode.NOT_FOUND)
    }

    // 지원하지 않는 HTTP Method
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(
        e: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        logException(LogEvent.BUSINESS_ERROR, e, request)

        return errorResponse(GlobalErrorCode.METHOD_NOT_ALLOWED)
    }

    // 지정한 형식으로 응답할 수 없음
    @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
    fun handleNotAcceptable(
        e: HttpMediaTypeNotAcceptableException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        logException(LogEvent.BUSINESS_ERROR, e, request)

        return errorResponse(GlobalErrorCode.NOT_ACCEPTABLE)
    }

    // 지원하지 않는 Content-Type
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleUnsupportedMediaType(
        e: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        logException(LogEvent.BUSINESS_ERROR, e, request)

        return errorResponse(GlobalErrorCode.UNSUPPORTED_MEDIA_TYPE)
    }

    // 예기치 못한 에러
    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse> {
        logException(LogEvent.UNEXPECTED_ERROR, e, request)

        return errorResponse(GlobalErrorCode.INTERNAL_ERROR)
    }

    private fun logException(
        event: LogEvent,
        e: Exception,
        request: HttpServletRequest,
    ) {
        val traceId = MDC.get("traceId")
        val userId = MDC.get("userId")
        val clientIp = MDC.get("clientIp")

        log.error {
            objectMapper.writeValueAsString(
                ErrorLog(
                    event = event.name,
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
    }

    private fun errorResponse(errorCode: ErrorCode): ResponseEntity<ApiResponse> {
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
}
