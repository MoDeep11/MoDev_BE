package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.SignupRequest
import modeep.modev.domain.auth.controller.dto.response.SignupResponse
import modeep.modev.domain.user.entity.User
import modeep.modev.domain.user.entity.UserStatus
import modeep.modev.domain.user.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignupService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun execute(request: SignupRequest): SignupResponse {
        validatePassword(request.password)

        if (request.password != request.passwordConfirm) {
            throw BusinessException(AuthErrorCode.PASSWORD_MISMATCH)
        }

        val email = request.email.trim().lowercase()
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw BusinessException(AuthErrorCode.EMAIL_ALREADY_EXISTS)
        }

        val user =
            User(
                email = email,
                passwordHash = passwordEncoder.encode(request.password),
                status = UserStatus.UNVERIFIED,
            )

        return try {
            SignupResponse.from(userRepository.saveAndFlush(user))
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
