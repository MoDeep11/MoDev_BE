package modeep.modev.global.security.jwt

import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI.removePrefix(request.contextPath)
        return path == TOKEN_REFRESH_PATH ||
            path == "$TOKEN_REFRESH_PATH/" ||
            path.endsWith(TOKEN_REFRESH_PATH) ||
            path.endsWith("$TOKEN_REFRESH_PATH/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)
        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val principal = jwtTokenProvider.parseAccessToken(token)
            val userId = principal.userId
            val authentication =
                UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_USER")),
                )

            SecurityContextHolder.getContext().authentication = authentication
            MDC.put("userId", userId)
            filterChain.doFilter(request, response)
        } catch (exception: JwtException) {
            SecurityContextHolder.clearContext()
            jwtAuthenticationEntryPoint.commence(request, response, exception)
        } catch (exception: IllegalArgumentException) {
            SecurityContextHolder.clearContext()
            jwtAuthenticationEntryPoint.commence(request, response, exception)
        } finally {
            MDC.remove("userId")
        }
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val authorization = request.getHeader(AUTHORIZATION_HEADER) ?: return null
        if (!authorization.startsWith(BEARER_PREFIX, ignoreCase = true)) {
            return null
        }

        return authorization.substring(BEARER_PREFIX.length).trim().takeIf { it.isNotBlank() }
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer "
        const val TOKEN_REFRESH_PATH = "/auth/token/refresh"
    }
}
