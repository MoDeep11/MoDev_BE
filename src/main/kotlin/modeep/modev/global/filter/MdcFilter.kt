package modeep.modev.global.filter

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import modeep.modev.global.response.InfoLog
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class MdcFilter(
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    private val log = KotlinLogging.logger {}

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val startedAt = System.currentTimeMillis()

        try {
            MDC.put("traceId", UUID.randomUUID().toString())
            MDC.put("clientIp", request.remoteAddr)
            MDC.put("requestUri", request.requestURI)
            // userId는 security 관련 필터에서 설정

            filterChain.doFilter(request, response)
        } finally {
            val userId = request.getAttribute(USER_ID_ATTRIBUTE) as? String
            val traceId = MDC.get("traceId")
            val clientIp = MDC.get("clientIp")
            if (!userId.isNullOrBlank()) {
                MDC.put("userId", userId)
            }
            val infoLog =
                objectMapper.writeValueAsString(
                    InfoLog(
                        traceId = traceId,
                        userId = userId,
                        clientIp = clientIp,
                        method = request.method,
                        path = request.requestURI,
                        query = request.requestURI.withQueryString(request.queryString),
                        status = response.status.toString(),
                        durationMs = (System.currentTimeMillis() - startedAt).toString(),
                    ),
                )
            log.info { infoLog }
            MDC.clear()
        }
    }

    private fun String.withQueryString(queryString: String?): String =
        if (queryString.isNullOrBlank()) {
            this
        } else {
            "$this?$queryString"
        }

    companion object {
        const val USER_ID_ATTRIBUTE = "userId"
    }
}
