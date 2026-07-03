package modeep.modev.domain.auth.service

import modeep.modev.global.security.jwt.RefreshTokenStore
import org.springframework.stereotype.Service

@Service
class LogoutService(
    private val refreshTokenStore: RefreshTokenStore,
) {
    fun execute(refreshToken: String) {
        if (refreshToken.isNotBlank()) {
            refreshTokenStore.delete(refreshToken)
        }
    }
}
