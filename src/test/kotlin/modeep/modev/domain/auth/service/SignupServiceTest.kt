package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.SignupRequest
import modeep.modev.domain.user.entity.User
import modeep.modev.domain.user.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.security.jwt.JwtTokenProvider
import modeep.modev.global.security.jwt.RefreshTokenStore
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SignupServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var refreshTokenStore: RefreshTokenStore
    private lateinit var signupService: SignupService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        jwtTokenProvider = mock(JwtTokenProvider::class.java)
        refreshTokenStore = mock(RefreshTokenStore::class.java)
        signupService = SignupService(userRepository, passwordEncoder, jwtTokenProvider, refreshTokenStore)
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
        val savedUser =
            User(
                id = 1L,
                email = "user@example.com",
                passwordHash = "encoded-password",
            )
        `when`(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(User::class.java)))
            .thenReturn(savedUser)
        `when`(jwtTokenProvider.generateAccessToken(savedUser)).thenReturn("access-token")
        `when`(jwtTokenProvider.generateRefreshToken(savedUser)).thenReturn("refresh-token")
        `when`(jwtTokenProvider.accessTokenExpiresInSeconds).thenReturn(3600)
        `when`(jwtTokenProvider.refreshTokenExpirationMillis).thenReturn(1209600000)

        val response = signupService.execute(request)

        assertEquals("access-token", response.accessToken)
        assertEquals(3600, response.expiresIn)
        assertEquals("refresh-token", response.refreshToken)
        assertEquals(1L, response.user.userId)
        assertEquals("user@example.com", response.user.email)
        assertEquals("UNVERIFIED", response.user.status)
        verify(passwordEncoder).encode("Password1!")
        verify(refreshTokenStore).save(
            refreshToken = "refresh-token",
            email = "user@example.com",
            ttl = Duration.ofDays(14),
        )
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
        verifyNoInteractions(userRepository, passwordEncoder, jwtTokenProvider, refreshTokenStore)
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
        verifyNoInteractions(userRepository, passwordEncoder, jwtTokenProvider, refreshTokenStore)
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
