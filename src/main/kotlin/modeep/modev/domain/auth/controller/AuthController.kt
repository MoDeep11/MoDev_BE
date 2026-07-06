package modeep.modev.domain.auth.controller

import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import modeep.modev.domain.auth.controller.dto.request.LoginRequest
import modeep.modev.domain.auth.controller.dto.request.SendEmailRequest
import modeep.modev.domain.auth.controller.dto.request.SignupRequest
import modeep.modev.domain.auth.controller.dto.request.VerifyEmailResponse
import modeep.modev.domain.auth.service.CookieService
import modeep.modev.domain.auth.service.CookieService.Companion.REFRESH_TOKEN_COOKIE
import modeep.modev.domain.auth.service.LoginService
import modeep.modev.domain.auth.service.LogoutService
import modeep.modev.domain.auth.service.SendEmailService
import modeep.modev.domain.auth.service.SignupService
import modeep.modev.domain.auth.service.TokenRefreshService
import modeep.modev.domain.auth.service.VerifyEmailService
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val signupService: SignupService,
    private val loginService: LoginService,
    private val logoutService: LogoutService,
    private val tokenRefreshService: TokenRefreshService,
    private val sendEmailService: SendEmailService,
    private val verifyEmailService: VerifyEmailService,
    private val cookieService: CookieService,
) : AuthControllerDocs {
    @PostMapping("/signup")
    override fun signup(
        @Valid @RequestBody request: SignupRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ApiResponse> {
        val (loginResponse, token) = signupService.execute(request)
        cookieService.addRefreshTokenCookie(response, token)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse(
                    success = true,
                    data = loginResponse,
                ),
            )
    }

    @PostMapping("/login")
    override fun login(
        @Valid @RequestBody request: LoginRequest,
        response: HttpServletResponse,
    ): ResponseEntity<ApiResponse> {
        val (loginResponse, token) = loginService.execute(request)
        cookieService.addRefreshTokenCookie(response, token)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = loginResponse,
            ),
        )
    }

    @PostMapping("/token/refresh")
    override fun refreshToken(
        @CookieValue(name = REFRESH_TOKEN_COOKIE, defaultValue = "") refreshToken: String,
        response: HttpServletResponse,
    ): ResponseEntity<ApiResponse> {
        if (refreshToken.isBlank()) {
            throw BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID)
        }

        val (tokenResponse, token) = tokenRefreshService.execute(refreshToken)
        cookieService.addRefreshTokenCookie(response, token)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = tokenResponse,
            ),
        )
    }

    @PostMapping("/email/send")
    override fun sendVerificationCode(
        @Valid @RequestBody request: SendEmailRequest,
    ): ResponseEntity<ApiResponse> {
        sendEmailService.execute(request)
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = null,
            ),
        )
    }

    @PostMapping("/email/verify")
    override fun verifyAuthCode(
        @Valid @RequestBody request: VerifyEmailResponse,
    ): ResponseEntity<ApiResponse> {
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = verifyEmailService.execute(request),
                error = null,
            ),
        )
    }

    @PostMapping("/logout")
    override fun logout(
        @CookieValue(name = REFRESH_TOKEN_COOKIE, defaultValue = "") refreshToken: String,
        response: HttpServletResponse,
    ): ResponseEntity<ApiResponse> {
        logoutService.execute(refreshToken)
        cookieService.clearRefreshTokenCookie(response)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = null,
            ),
        )
    }
}
