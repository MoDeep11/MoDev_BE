package modeep.modev.domain.auth.controller

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import modeep.modev.domain.auth.controller.dto.request.EmailVerificationSendRequest
import modeep.modev.domain.auth.controller.dto.request.LoginRequest
import modeep.modev.domain.auth.controller.dto.request.SignupRequest
import modeep.modev.domain.auth.controller.dto.response.LoginResponse
import modeep.modev.domain.auth.service.EmailVerificationSendService
import modeep.modev.domain.auth.service.LoginService
import modeep.modev.domain.auth.service.SignupService
import modeep.modev.domain.auth.service.TokenRefreshService
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.response.ApiResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val signupService: SignupService,
    private val loginService: LoginService,
    private val tokenRefreshService: TokenRefreshService,
    private val emailVerificationSendService: EmailVerificationSendService,
) {
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(
        @RequestBody request: SignupRequest,
    ): ApiResponse =
        ApiResponse(
            success = true,
            data = signupService.execute(request),
        )

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ApiResponse {
        val loginResponse = loginService.execute(request)
        setRefreshTokenCookie(response, loginResponse)
        return ApiResponse(
            success = true,
            data = loginResponse,
        )
    }

    @PostMapping("/token/refresh")
    fun refreshToken(
        @CookieValue(name = REFRESH_TOKEN_COOKIE, defaultValue = "") refreshToken: String,
        response: HttpServletResponse,
    ): ApiResponse {
        if (refreshToken.isBlank()) {
            throw BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID)
        }

        val loginResponse = tokenRefreshService.execute(refreshToken)
        setRefreshTokenCookie(response, loginResponse)
        return ApiResponse(
            success = true,
            data = loginResponse,
        )
    }

    @PostMapping("/email/resend")
    fun sendVerificationEmail(
        @Valid @RequestBody request: EmailVerificationSendRequest,
    ): ApiResponse {
        emailVerificationSendService.execute(request)
        return ApiResponse(
            success = true,
            data = null,
        )
    }

    @PostMapping("/logout")
    fun logout(): ApiResponse =
        ApiResponse(
            success = true,
            data = null,
        )

    private fun setRefreshTokenCookie(
        response: HttpServletResponse,
        loginResponse: LoginResponse,
    ) {
        val cookie =
            ResponseCookie
                .from(REFRESH_TOKEN_COOKIE, loginResponse.refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(REFRESH_TOKEN_PATH)
                .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private companion object {
        const val REFRESH_TOKEN_COOKIE = "refreshToken"
        const val REFRESH_TOKEN_PATH = "/auth/token/refresh"
    }
}
