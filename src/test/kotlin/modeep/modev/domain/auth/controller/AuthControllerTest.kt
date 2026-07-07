package modeep.modev.domain.auth.controller

import jakarta.servlet.http.HttpServletResponse
import modeep.modev.domain.auth.controller.dto.request.SendEmailRequest
import modeep.modev.domain.auth.service.CookieService
import modeep.modev.domain.auth.service.LoginService
import modeep.modev.domain.auth.service.LogoutService
import modeep.modev.domain.auth.service.SendEmailService
import modeep.modev.domain.auth.service.SignupService
import modeep.modev.domain.auth.service.TokenRefreshService
import modeep.modev.domain.auth.service.VerifyEmailService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthControllerTest {
    private val verifyEmailService = mock(VerifyEmailService::class.java)
    private val sendEmailService = mock(SendEmailService::class.java)
    private val logoutService = mock(LogoutService::class.java)
    private val cookieService = mock(CookieService::class.java)
    private val controller =
        AuthController(
            signupService = mock(SignupService::class.java),
            loginService = mock(LoginService::class.java),
            logoutService = logoutService,
            tokenRefreshService = mock(TokenRefreshService::class.java),
            sendEmailService = sendEmailService,
            verifyEmailService = verifyEmailService,
            cookieService = cookieService,
        )

    @Test
    fun `send verification code delegates to service and returns success`() {
        val request = SendEmailRequest("user@example.com")

        val response = controller.sendVerificationCode(request)
        val body = requireNotNull(response.body)

        verify(sendEmailService).execute(request)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(body.success)
        assertNull(body.data)
        assertNull(body.error)
    }

    @Test
    fun `logout deletes refresh token and expires cookies`() {
        val servletResponse = mock(HttpServletResponse::class.java)

        val response = controller.logout("refresh-token", "Bearer access-token", servletResponse)
        val body = requireNotNull(response.body)

        verify(logoutService).execute("refresh-token", "access-token")
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(body.success)
        assertNull(body.data)
        assertNull(body.error)
        verify(cookieService).clearRefreshTokenCookie(servletResponse)
    }
}
