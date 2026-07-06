package modeep.modev.global.ratelimit

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import modeep.modev.global.exception.error.GlobalErrorCode
import modeep.modev.global.response.ApiResponse
import modeep.modev.global.response.ErrorResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RateLimitFilter(
    private val rateLimitService: RateLimitService,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val target = request.resolveRateLimitTarget()

        if (target == null) {
            filterChain.doFilter(request, response)
            return
        }

        val allowed =
            target.policies.all { policy ->
                rateLimitService.tryConsume(target.key, policy)
            }

        if (!allowed) {
            val errorCode = GlobalErrorCode.TOO_MANY_REQUESTS

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
            return
        }
        filterChain.doFilter(request, response)
    }
}
