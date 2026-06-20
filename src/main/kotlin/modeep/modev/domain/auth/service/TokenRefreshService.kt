package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.response.LoginResponse
import modeep.modev.domain.auth.controller.dto.response.LoginUserResponse
import modeep.modev.domain.auth.entity.UserStatus
import modeep.modev.domain.auth.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.security.jwt.JwtTokenProvider
import modeep.modev.global.security.jwt.RefreshTokenStore
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
    fun execute(refreshToken: String): LoginResponse {
        val email = jwtTokenProvider.parseRefreshToken(refreshToken)
        val user =
            userRepository.findByEmailIgnoreCase(email)
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

        return LoginResponse(
            accessToken = jwtTokenProvider.generateAccessToken(user),
            refreshToken = newRefreshToken,
            expiresIn = jwtTokenProvider.accessTokenExpiresInSeconds,
            refreshExpiresIn = jwtTokenProvider.refreshTokenExpiresInSeconds,
            user = LoginUserResponse.from(user),
        )
    }
}
