package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.repository.AccessTokenBlacklistStore
import modeep.modev.domain.auth.repository.RefreshTokenStore
import modeep.modev.global.security.jwt.JwtTokenProvider
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class LogoutService(
    private val refreshTokenStore: RefreshTokenStore,
    private val accessTokenBlacklistStore: AccessTokenBlacklistStore,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    fun execute(
        refreshToken: String,
        accessToken: String?,
    ) {
        if (refreshToken.isNotBlank()) {
            refreshTokenStore.delete(refreshToken)
        }

        if (!accessToken.isNullOrBlank()) {
            val expiresIn = jwtTokenProvider.getRemainingExpirationMillis(accessToken)
            accessTokenBlacklistStore.save(accessToken, Duration.ofMillis(expiresIn))
        }
    }
}
