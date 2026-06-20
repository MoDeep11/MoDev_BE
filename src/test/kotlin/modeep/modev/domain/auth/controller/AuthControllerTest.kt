package modeep.modev.domain.auth.controller

import modeep.modev.domain.auth.controller.dto.request.EmailVerificationSendRequest
import modeep.modev.domain.auth.service.LoginService
import modeep.modev.domain.auth.service.SignupService
import modeep.modev.domain.auth.service.TokenRefreshService
import modeep.modev.global.mail.MailService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthControllerTest {
    private val mailService = mock(MailService::class.java)
    private val controller =
        AuthController(
            signupService = mock(SignupService::class.java),
            loginService = mock(LoginService::class.java),
            tokenRefreshService = mock(TokenRefreshService::class.java),
            mailService = mailService,
        )

    @Test
    fun `send verification code delegates to service and returns success`() {
        val request = EmailVerificationSendRequest("user@example.com")

        val response = controller.sendVerificationCode(request)

        verify(mailService).sendVerificationCode(request)
        assertTrue(response.success)
        assertNull(response.data)
        assertNull(response.error)
    }

    @Test
    fun `logout returns success api response with null data`() {
        val response = controller.logout()

        assertTrue(response.success)
        assertNull(response.data)
        assertNull(response.error)
    }
}
