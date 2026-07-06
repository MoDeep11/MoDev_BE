package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.SignupRequest
import modeep.modev.domain.auth.controller.dto.response.LoginResponse
import modeep.modev.domain.auth.service.VerifyEmailService.Companion.VERIFIED_KEY_PREFIX
import modeep.modev.domain.auth.service.VerifyEmailService.Companion.VERIFIED_VALUE
import modeep.modev.domain.user.entity.User
import modeep.modev.domain.user.entity.UserStatus
import modeep.modev.domain.user.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignupService(
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val passwordEncoder: PasswordEncoder,
    private val loginService: LoginService,
) {
    @Transactional
    fun execute(request: SignupRequest): Pair<LoginResponse, String> {
        validatePassword(request.password)

        if (request.password != request.passwordConfirm) {
            throw BusinessException(AuthErrorCode.PASSWORD_MISMATCH)
        }

        val email = request.email.trim().lowercase()
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw BusinessException(AuthErrorCode.EMAIL_ALREADY_EXISTS)
        }

        val verifiedKey = "$VERIFIED_KEY_PREFIX$email"
        if (redisTemplate.opsForValue().get(verifiedKey) != VERIFIED_VALUE) {
            throw BusinessException(AuthErrorCode.EMAIL_NOT_VERIFIED)
        }

        val user =
            User(
                email = email,
                passwordHash = passwordEncoder.encode(request.password),
                status = UserStatus.ACTIVE,
            )

        return try {
            val saved = userRepository.saveAndFlush(user)
            redisTemplate.delete(verifiedKey)
            loginService.buildLoginResponse(saved)
        } catch (exception: DataIntegrityViolationException) {
            throw BusinessException(
                errorCode = AuthErrorCode.EMAIL_ALREADY_EXISTS,
                cause = exception,
            )
        }
    }

    private fun validatePassword(password: String) {
        if (!PASSWORD_PATTERN.matches(password)) {
            throw BusinessException(AuthErrorCode.INVALID_PASSWORD_FORMAT)
        }
    }

    companion object {
        private val PASSWORD_PATTERN =
            Regex("""^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d\s])\S{8,72}$""")
    }
}
