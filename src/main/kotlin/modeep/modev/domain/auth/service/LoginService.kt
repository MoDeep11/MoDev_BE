package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.LoginRequest
import modeep.modev.domain.auth.controller.dto.response.LoginResponse
import modeep.modev.domain.auth.controller.dto.response.LoginUserResponse
import modeep.modev.domain.auth.entity.UserStatus
import modeep.modev.domain.auth.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.security.jwt.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @Transactional(readOnly = true)
    fun execute(request: LoginRequest): LoginResponse {
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

        return LoginResponse(
            accessToken = jwtTokenProvider.generateAccessToken(user),
            expiresIn = jwtTokenProvider.accessTokenExpiresInSeconds,
            user = LoginUserResponse.from(user),
        )
    }
}
