package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.SignupRequest
import modeep.modev.domain.auth.repository.RefreshTokenStore
import modeep.modev.domain.user.entity.User
import modeep.modev.domain.user.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.security.jwt.JwtTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SignupServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var refreshTokenStore: RefreshTokenStore
    private lateinit var loginService: LoginService
    private lateinit var signupService: SignupService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        jwtTokenProvider =
            JwtTokenProvider(
                secret = "01234567890123456789012345678901",
                accessTokenExpiration = 3600000,
                refreshTokenExpiration = 1209600000,
            )
        refreshTokenStore = mock(RefreshTokenStore::class.java)
        loginService = LoginService(userRepository, passwordEncoder, jwtTokenProvider, refreshTokenStore)
        signupService = SignupService(userRepository, passwordEncoder, loginService)
    }

    @Test
    fun `creates an unverified user`() {
        val request =
            SignupRequest(
                email = " User@Example.com ",
                password = "Password1!",
                passwordConfirm = "Password1!",
            )
        `when`(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false)
        `when`(passwordEncoder.encode("Password1!")).thenReturn("encoded-password")
        `when`(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(User::class.java)))
            .thenAnswer { it.arguments[0] as User }

        val (response, refreshToken) = signupService.execute(request)

        assertEquals("user@example.com", response.user.email)
        assertEquals("UNVERIFIED", response.user.status)
        assertEquals(3600, response.expiresIn)
        assertTrue(response.accessToken.isNotBlank())
        assertTrue(refreshToken.isNotBlank())
        verify(passwordEncoder).encode("Password1!")
    }

    @Test
    fun `rejects invalid password format`() {
        val exception =
            assertFailsWith<BusinessException> {
                signupService.execute(
                    SignupRequest(
                        email = "user@example.com",
                        password = "password",
                        passwordConfirm = "password",
                    ),
                )
            }

        assertEquals(AuthErrorCode.INVALID_PASSWORD_FORMAT, exception.errorCode)
        verifyNoInteractions(userRepository, passwordEncoder)
    }

    @Test
    fun `rejects password mismatch`() {
        val exception =
            assertFailsWith<BusinessException> {
                signupService.execute(
                    SignupRequest(
                        email = "user@example.com",
                        password = "Password1!",
                        passwordConfirm = "Password2!",
                    ),
                )
            }

        assertEquals(AuthErrorCode.PASSWORD_MISMATCH, exception.errorCode)
        verifyNoInteractions(userRepository, passwordEncoder)
    }

    @Test
    fun `rejects duplicate email`() {
        `when`(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true)

        val exception =
            assertFailsWith<BusinessException> {
                signupService.execute(
                    SignupRequest(
                        email = "User@Example.com",
                        password = "Password1!",
                        passwordConfirm = "Password1!",
                    ),
                )
            }

        assertEquals(AuthErrorCode.EMAIL_ALREADY_EXISTS, exception.errorCode)
        verifyNoInteractions(passwordEncoder)
    }
}
