package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.response.TokenRefreshResponse
import modeep.modev.domain.auth.repository.RefreshTokenStore
import modeep.modev.domain.user.entity.UserStatus
import modeep.modev.domain.user.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.security.jwt.JwtTokenProvider
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
    fun execute(refreshToken: String): Pair<TokenRefreshResponse, String> {
        val userId =
            jwtTokenProvider.parseRefreshToken(refreshToken).toLongOrNull()
                ?: throw BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID)
        val user =
            userRepository.findUserById(userId)
                ?: throw BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID)

        when (user.status) {
            UserStatus.LOCKED -> throw BusinessException(AuthErrorCode.ACCOUNT_LOCKED)
            UserStatus.UNVERIFIED -> throw BusinessException(AuthErrorCode.EMAIL_NOT_VERIFIED)
            UserStatus.ACTIVE -> Unit
        }

        val newRefreshToken = jwtTokenProvider.generateRefreshToken(user)

        // 동시성 방지 및 재사용 방지
        val rotated =
            refreshTokenStore.rotate(
                currentRefreshToken = refreshToken,
                newRefreshToken = newRefreshToken,
                userId = user.id.toString(),
                ttl = Duration.ofMillis(jwtTokenProvider.refreshTokenExpirationMillis),
            )
        if (!rotated) {
            throw BusinessException(AuthErrorCode.REFRESH_TOKEN_REUSED)
        }

        val response =
            TokenRefreshResponse(
                accessToken = jwtTokenProvider.generateAccessToken(user),
                expiresIn = jwtTokenProvider.accessTokenExpiresInSeconds,
            )

        return Pair(response, newRefreshToken)
    }
}
