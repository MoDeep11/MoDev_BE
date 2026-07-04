package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.response.TokenRefreshResponse
import modeep.modev.domain.user.entity.UserStatus
import modeep.modev.domain.user.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.security.jwt.JwtTokenProvider
import modeep.modev.global.security.jwt.RefreshTokenStore
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class TokenRefreshService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenStore: RefreshTokenStore,
) {
    @Transactional(readOnly = true)
    fun execute(
        refreshToken: String,
        accessToken: String,
    ): TokenRefreshResponse {
        val refreshTokenUserId = jwtTokenProvider.parseRefreshToken(refreshToken)
        val accessTokenUserId = jwtTokenProvider.parseAccessTokenForRefresh(accessToken).userId
        if (refreshTokenUserId != accessTokenUserId) {
            throw BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID)
        }

        val userId =
            refreshTokenUserId.toLongOrNull()
                ?: throw BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID)
        val user =
            userRepository.findByIdOrNull(userId)
                ?: throw BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID)

        when (user.status) {
            UserStatus.LOCKED -> throw BusinessException(AuthErrorCode.ACCOUNT_LOCKED)
            UserStatus.UNVERIFIED -> throw BusinessException(AuthErrorCode.EMAIL_NOT_VERIFIED)
            UserStatus.ACTIVE -> Unit
        }

        val newRefreshToken = jwtTokenProvider.generateRefreshToken(user)
        val rotated =
            refreshTokenStore.rotate(
                currentRefreshToken = refreshToken,
                newRefreshToken = newRefreshToken,
                email = user.email,
                ttl = Duration.ofMillis(jwtTokenProvider.refreshTokenExpirationMillis),
            )
        if (!rotated) {
            throw BusinessException(AuthErrorCode.REFRESH_TOKEN_REUSED)
        }

        return TokenRefreshResponse.from(
            accessToken = jwtTokenProvider.generateAccessToken(user),
            refreshToken = newRefreshToken,
            expiresIn = jwtTokenProvider.accessTokenExpiresInSeconds,
        )
    }
}
