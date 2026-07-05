package modeep.modev.global.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import modeep.modev.global.exception.error.GlobalErrorCode
import modeep.modev.global.response.ApiResponse
import modeep.modev.global.response.ErrorResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        writeUnauthorized(response)
    }

    fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: Exception,
    ) {
        writeUnauthorized(response)
    }

    private fun writeUnauthorized(response: HttpServletResponse) {
        val errorCode = GlobalErrorCode.UNAUTHORIZED

        response.status = errorCode.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        objectMapper.writeValue(
            response.writer,
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
