package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.repository.AccessTokenBlacklistStore
import modeep.modev.domain.auth.repository.RefreshTokenStore
import modeep.modev.global.security.jwt.JwtTokenProvider
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.time.Duration

class LogoutServiceTest {
    private val refreshTokenStore = mock(RefreshTokenStore::class.java)
    private val accessTokenBlacklistStore = mock(AccessTokenBlacklistStore::class.java)
    private val jwtTokenProvider = mock(JwtTokenProvider::class.java)
    private val logoutService = LogoutService(refreshTokenStore, accessTokenBlacklistStore, jwtTokenProvider)

    @Test
    fun `deletes refresh token and blacklists access token`() {
        `when`(jwtTokenProvider.getRemainingExpirationMillis("access-token")).thenReturn(3600000)

        logoutService.execute("refresh-token", "access-token")

        verify(refreshTokenStore).delete("refresh-token")
        verify(accessTokenBlacklistStore).save("access-token", Duration.ofMillis(3600000))
    }

    @Test
    fun `does not delete when refresh token cookie is missing`() {
        logoutService.execute("", null)

        verifyNoInteractions(refreshTokenStore, accessTokenBlacklistStore, jwtTokenProvider)
    }
}
