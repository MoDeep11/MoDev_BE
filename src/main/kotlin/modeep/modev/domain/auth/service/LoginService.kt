package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.LoginRequest
import modeep.modev.domain.auth.controller.dto.response.LoginResponse
import modeep.modev.domain.auth.controller.dto.response.LoginUserResponse
import modeep.modev.domain.auth.repository.RefreshTokenStore
import modeep.modev.domain.user.entity.User
import modeep.modev.domain.user.entity.UserStatus
import modeep.modev.domain.user.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.security.jwt.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class LoginService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenStore: RefreshTokenStore,
) {
    @Transactional(readOnly = true)
    fun execute(request: LoginRequest): Pair<LoginResponse, String> {
        val user =
            userRepository.findByEmailIgnoreCase(request.email.trim().lowercase())
                ?: throw BusinessException(AuthErrorCode.INVALID_CREDENTIALS)

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw BusinessException(AuthErrorCode.INVALID_CREDENTIALS)
        }

        when (user.status) {
            UserStatus.LOCKED -> throw BusinessException(AuthErrorCode.ACCOUNT_LOCKED)
            UserStatus.UNVERIFIED -> throw BusinessException(AuthErrorCode.EMAIL_NOT_VERIFIED)
            UserStatus.ACTIVE -> Unit
        }

        return buildLoginResponse(user)
    }

    // LoginResponse와 RefreshToken 쌍을 반환합니다.
    fun buildLoginResponse(user: User): Pair<LoginResponse, String> {
        val refreshToken = jwtTokenProvider.generateRefreshToken(user)
        refreshTokenStore.save(
            refreshToken = refreshToken,
            userId = user.id.toString(),
            ttl = Duration.ofMillis(jwtTokenProvider.refreshTokenExpirationMillis),
        )

        val response =
            LoginResponse(
                accessToken = jwtTokenProvider.generateAccessToken(user),
                expiresIn = jwtTokenProvider.accessTokenExpiresInSeconds,
                user = LoginUserResponse.from(user),
            )

        return Pair(response, refreshToken)
    }
}
