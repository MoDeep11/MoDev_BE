package modeep.modev.domain.auth.service

import modeep.modev.domain.user.entity.User
import modeep.modev.domain.user.entity.UserStatus
import modeep.modev.domain.user.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.security.jwt.JwtTokenProvider
import modeep.modev.global.security.jwt.RefreshTokenStore
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TokenRefreshServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var refreshTokenStore: RefreshTokenStore
    private lateinit var tokenRefreshService: TokenRefreshService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        jwtTokenProvider = mock(JwtTokenProvider::class.java)
        refreshTokenStore = mock(RefreshTokenStore::class.java)
        tokenRefreshService = TokenRefreshService(userRepository, jwtTokenProvider, refreshTokenStore)
    }

    @Test
    fun `refreshes access token`() {
        val user = activeUser()
        `when`(jwtTokenProvider.parseRefreshToken("refresh-token")).thenReturn(user.email)
        `when`(userRepository.findByEmailIgnoreCase(user.email)).thenReturn(user)
        `when`(jwtTokenProvider.generateAccessToken(user)).thenReturn("new-access-token")
        `when`(jwtTokenProvider.generateRefreshToken(user)).thenReturn("new-refresh-token")
        `when`(jwtTokenProvider.accessTokenExpiresInSeconds).thenReturn(3600)
        `when`(jwtTokenProvider.refreshTokenExpiresInSeconds).thenReturn(1209600)
        `when`(jwtTokenProvider.refreshTokenExpirationMillis).thenReturn(1209600000)
        `when`(
            refreshTokenStore.rotate(
                "refresh-token",
                "new-refresh-token",
                user.email,
                java.time.Duration.ofDays(14),
            ),
        ).thenReturn(true)

        val response = tokenRefreshService.execute("refresh-token")

        assertEquals("new-access-token", response.accessToken)
        assertEquals("new-refresh-token", response.refreshToken)
        assertEquals("Bearer", response.tokenType)
        assertEquals(3600, response.expiresIn)
        assertEquals(1209600, response.refreshExpiresIn)
        assertEquals(user.email, response.user.email)
    }

    @Test
    fun `rejects refresh token for missing user`() {
        `when`(jwtTokenProvider.parseRefreshToken("refresh-token")).thenReturn("missing@example.com")
        `when`(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(null)

        val exception =
            assertFailsWith<BusinessException> {
                tokenRefreshService.execute("refresh-token")
            }

        assertEquals(AuthErrorCode.REFRESH_TOKEN_INVALID, exception.errorCode)
    }

    @Test
    fun `rejects locked user`() {
        val user = activeUser(status = UserStatus.LOCKED)
        `when`(jwtTokenProvider.parseRefreshToken("refresh-token")).thenReturn(user.email)
        `when`(userRepository.findByEmailIgnoreCase(user.email)).thenReturn(user)

        val exception =
            assertFailsWith<BusinessException> {
                tokenRefreshService.execute("refresh-token")
            }

        assertEquals(AuthErrorCode.ACCOUNT_LOCKED, exception.errorCode)
        verify(jwtTokenProvider, never()).generateAccessToken(user)
        verify(jwtTokenProvider, never()).generateRefreshToken(user)
    }

    @Test
    fun `rejects an already consumed refresh token`() {
        val user = activeUser()
        `when`(jwtTokenProvider.parseRefreshToken("refresh-token")).thenReturn(user.email)
        `when`(userRepository.findByEmailIgnoreCase(user.email)).thenReturn(user)
        `when`(jwtTokenProvider.generateRefreshToken(user)).thenReturn("new-refresh-token")
        `when`(jwtTokenProvider.refreshTokenExpirationMillis).thenReturn(1209600000)
        `when`(
            refreshTokenStore.rotate(
                "refresh-token",
                "new-refresh-token",
                user.email,
                java.time.Duration.ofDays(14),
            ),
        ).thenReturn(false)

        val exception =
            assertFailsWith<BusinessException> {
                tokenRefreshService.execute("refresh-token")
            }

        assertEquals(AuthErrorCode.REFRESH_TOKEN_REUSED, exception.errorCode)
        verify(jwtTokenProvider, never()).generateAccessToken(user)
    }

    private fun activeUser(status: UserStatus = UserStatus.ACTIVE) =
        User(
            id = 1L,
            email = "user@example.com",
            passwordHash = "encoded-password",
            status = status,
        )
}
