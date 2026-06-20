package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.LoginRequest
import modeep.modev.domain.auth.entity.User
import modeep.modev.domain.auth.entity.UserStatus
import modeep.modev.domain.auth.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.security.jwt.JwtTokenProvider
import modeep.modev.global.security.jwt.RefreshTokenStore
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LoginServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var refreshTokenStore: RefreshTokenStore
    private lateinit var loginService: LoginService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        jwtTokenProvider = mock(JwtTokenProvider::class.java)
        refreshTokenStore = mock(RefreshTokenStore::class.java)
        loginService = LoginService(userRepository, passwordEncoder, jwtTokenProvider, refreshTokenStore)
    }

    @Test
    fun `logs in an active user`() {
        val user = activeUser()
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(user)
        `when`(passwordEncoder.matches("Password1!", user.passwordHash)).thenReturn(true)
        `when`(jwtTokenProvider.generateAccessToken(user)).thenReturn("access-token")
        `when`(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh-token")
        `when`(jwtTokenProvider.accessTokenExpiresInSeconds).thenReturn(3600)
        `when`(jwtTokenProvider.refreshTokenExpiresInSeconds).thenReturn(1209600)
        `when`(jwtTokenProvider.refreshTokenExpirationMillis).thenReturn(1209600000)

        val response =
            loginService.execute(
                LoginRequest(
                    email = " User@Example.com ",
                    password = "Password1!",
                ),
            )

        assertEquals("access-token", response.accessToken)
        assertEquals("Bearer", response.tokenType)
        assertEquals(3600, response.expiresIn)
        assertEquals(1L, response.user.userId)
        assertEquals("user@example.com", response.user.email)
        assertEquals("ACTIVE", response.user.status)
    }

    @Test
    fun `rejects an unknown email`() {
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(null)

        val exception =
            assertFailsWith<BusinessException> {
                loginService.execute(LoginRequest("user@example.com", "Password1!"))
            }

        assertEquals(AuthErrorCode.INVALID_CREDENTIALS, exception.errorCode)
        verifyNoInteractions(passwordEncoder, jwtTokenProvider)
    }

    @Test
    fun `rejects an invalid password`() {
        val user = activeUser()
        `when`(userRepository.findByEmailIgnoreCase(user.email)).thenReturn(user)
        `when`(passwordEncoder.matches("WrongPassword1!", user.passwordHash)).thenReturn(false)

        val exception =
            assertFailsWith<BusinessException> {
                loginService.execute(LoginRequest(user.email, "WrongPassword1!"))
            }

        assertEquals(AuthErrorCode.INVALID_CREDENTIALS, exception.errorCode)
        verifyNoInteractions(jwtTokenProvider)
    }

    @Test
    fun `rejects a locked user`() {
        val user = activeUser(status = UserStatus.LOCKED)
        `when`(userRepository.findByEmailIgnoreCase(user.email)).thenReturn(user)
        `when`(passwordEncoder.matches("Password1!", user.passwordHash)).thenReturn(true)

        val exception =
            assertFailsWith<BusinessException> {
                loginService.execute(LoginRequest(user.email, "Password1!"))
            }

        assertEquals(AuthErrorCode.ACCOUNT_LOCKED, exception.errorCode)
        verifyNoInteractions(jwtTokenProvider)
    }

    @Test
    fun `rejects an unverified user`() {
        val user = activeUser(status = UserStatus.UNVERIFIED)
        `when`(userRepository.findByEmailIgnoreCase(user.email)).thenReturn(user)
        `when`(passwordEncoder.matches("Password1!", user.passwordHash)).thenReturn(true)

        val exception =
            assertFailsWith<BusinessException> {
                loginService.execute(LoginRequest(user.email, "Password1!"))
            }

        assertEquals(AuthErrorCode.EMAIL_NOT_VERIFIED, exception.errorCode)
        verifyNoInteractions(jwtTokenProvider)
    }

    private fun activeUser(status: UserStatus = UserStatus.ACTIVE) =
        User(
            id = 1L,
            email = "user@example.com",
            passwordHash = "encoded-password",
            status = status,
        )
}
