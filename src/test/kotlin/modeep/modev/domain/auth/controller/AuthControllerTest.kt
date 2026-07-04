package modeep.modev.domain.auth.controller

import jakarta.servlet.http.HttpServletResponse
import modeep.modev.domain.auth.controller.dto.request.EmailVerificationSendRequest
import modeep.modev.domain.auth.controller.dto.request.TokenRefreshRequest
import modeep.modev.domain.auth.controller.dto.response.TokenRefreshResponse
import modeep.modev.domain.auth.service.EmailVerificationService
import modeep.modev.domain.auth.service.LoginService
import modeep.modev.domain.auth.service.LogoutService
import modeep.modev.domain.auth.service.SignupService
import modeep.modev.domain.auth.service.TokenRefreshService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpHeaders
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthControllerTest {
    private val emailVerificationService = mock(EmailVerificationService::class.java)
    private val logoutService = mock(LogoutService::class.java)
    private val tokenRefreshService = mock(TokenRefreshService::class.java)
    private val controller =
        AuthController(
            signupService = mock(SignupService::class.java),
            loginService = mock(LoginService::class.java),
            logoutService = logoutService,
            tokenRefreshService = tokenRefreshService,
            emailVerificationService = emailVerificationService,
        )

    @Test
    fun `send verification code delegates to service and returns success`() {
        val request = EmailVerificationSendRequest("user@example.com")

        val response = controller.sendVerificationCode(request)

        verify(emailVerificationService).sendVerificationCode(request)
        assertTrue(response.success)
        assertNull(response.data)
        assertNull(response.error)
    }

    @Test
    fun `refresh token reads refresh token from body and access token from authorization header`() {
        val servletResponse = mock(HttpServletResponse::class.java)
        val tokenRefreshResponse =
            TokenRefreshResponse(
                accessToken = "new-access-token",
                refreshToken = "new-refresh-token",
                expiresIn = 3600,
            )
        `when`(tokenRefreshService.execute("refresh-token", "expired-access-token")).thenReturn(tokenRefreshResponse)

        val response =
            controller.refreshToken(
                authorization = "Bearer expired-access-token",
                request = TokenRefreshRequest("refresh-token"),
                response = servletResponse,
            )

        verify(tokenRefreshService).execute("refresh-token", "expired-access-token")
        assertTrue(response.success)
        assertEquals(tokenRefreshResponse, response.data)

        val cookieCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(servletResponse).addHeader(
            org.mockito.ArgumentMatchers.eq(HttpHeaders.SET_COOKIE),
            cookieCaptor.capture(),
        )
        assertTrue(cookieCaptor.value.contains("refreshToken=new-refresh-token"))
        assertTrue(cookieCaptor.value.contains("Path=/auth;"))
    }

    @Test
    fun `logout deletes refresh token and expires cookies`() {
        val servletResponse = mock(HttpServletResponse::class.java)

        val response = controller.logout("refresh-token", servletResponse)

        verify(logoutService).execute("refresh-token")
        assertTrue(response.success)
        assertNull(response.data)
        assertNull(response.error)

        val cookieCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(servletResponse, times(2)).addHeader(
            org.mockito.ArgumentMatchers.eq(HttpHeaders.SET_COOKIE),
            cookieCaptor.capture(),
        )
        val cookies = cookieCaptor.allValues
        assertEquals(2, cookies.size)
        assertTrue(cookies.any { it.contains("Path=/auth;") && it.contains("Max-Age=0") })
        assertTrue(cookies.any { it.contains("Path=/auth/token/refresh;") && it.contains("Max-Age=0") })
    }
}
