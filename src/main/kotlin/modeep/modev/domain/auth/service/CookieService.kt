package modeep.modev.domain.auth.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import modeep.modev.global.security.jwt.JwtTokenProvider
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CookieService(
    private val jwtTokenProvider: JwtTokenProvider,
) {
    fun addRefreshTokenCookie(
        response: HttpServletResponse,
        refreshToken: String,
    ) {
        val cookie =
            ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(REFRESH_PATH)
                .maxAge(refreshTokenMaxAge)
                .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    fun resolveRefreshToken(request: HttpServletRequest): String? =
        request.cookies
            ?.firstOrNull { it.name == REFRESH_TOKEN_COOKIE }
            ?.value

    fun clearRefreshTokenCookie(response: HttpServletResponse) {
        val cookie =
            expireRefreshTokenCookie(REFRESH_PATH)

        val legacyCookie =
            expireRefreshTokenCookie(LEGACY_REFRESH_PATH)

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, legacyCookie.toString())
    }

    private fun expireRefreshTokenCookie(path: String): ResponseCookie =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path(path)
            .maxAge(0)
            .build()

    val refreshTokenMaxAge: Duration = Duration.ofSeconds(jwtTokenProvider.refreshTokenExpiresInSeconds)

    companion object {
        const val REFRESH_TOKEN_COOKIE = "refresh_token"
        const val REFRESH_PATH = "/auth"
        const val LEGACY_REFRESH_PATH = "/auth/token/refresh"
    }
}
