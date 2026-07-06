package modeep.modev.global.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class MdcFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            MDC.put("traceId", UUID.randomUUID().toString())
            MDC.put("clientIp", request.remoteAddr)
            MDC.put("requestUri", request.requestURI)
            // userId는 security 관련 필터에서 설정

            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
