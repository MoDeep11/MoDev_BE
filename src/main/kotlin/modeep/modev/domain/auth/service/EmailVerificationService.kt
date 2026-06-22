package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.EmailVerificationSendRequest
import modeep.modev.domain.auth.controller.dto.request.VerifyCode
import modeep.modev.domain.auth.controller.dto.response.EmailVerificationResponse
import modeep.modev.domain.auth.entity.UserStatus
import modeep.modev.domain.auth.repository.UserRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.mail.EmailTemplateRenderer
import modeep.modev.global.mail.MailMessage
import modeep.modev.global.mail.MailService
import modeep.modev.global.mail.MailSubjects
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Duration

@Service
class EmailVerificationService(
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val emailTemplateRenderer: EmailTemplateRenderer,
    private val mailService: MailService,
) {
    @Transactional(readOnly = true)
    fun sendVerificationCode(request: EmailVerificationSendRequest) {
        val email = request.email.trim().lowercase()
        val user =
            userRepository.findByEmailIgnoreCase(email)
                ?: throw BusinessException(AuthErrorCode.USER_NOT_FOUND)

        when (user.status) {
            UserStatus.UNVERIFIED -> Unit
            UserStatus.LOCKED -> throw BusinessException(AuthErrorCode.ACCOUNT_LOCKED)
            UserStatus.ACTIVE -> throw BusinessException(AuthErrorCode.ALREADY_VERIFIED)
        }

        val rateLimitKey = "$RATE_LIMIT_KEY_PREFIX$email"
        val acquired = redisTemplate.opsForValue().setIfAbsent(rateLimitKey, "1", RATE_LIMIT_TTL)
        if (acquired != true) {
            throw BusinessException(AuthErrorCode.RESEND_RATE_LIMIT)
        }

        val code = generateVerificationCode()
        val codeKey = "$CODE_KEY_PREFIX$email"

        try {
            redisTemplate.opsForValue().set(codeKey, code, VERIFY_CODE_TTL)

            val body =
                emailTemplateRenderer.render(
                    templatePath = VERIFY_EMAIL_TEMPLATE_PATH,
                    variables =
                        code.mapIndexed { index, digit ->
                            "CODE_${index + 1}" to digit.toString()
                        }.toMap(),
                )

            mailService.send(
                MailMessage(
                    to = user.email,
                    subject = MailSubjects.VERIFY_EMAIL,
                    body = body,
                    isHtml = true,
                ),
            )
        } catch (exception: Exception) {
            redisTemplate.delete(codeKey)
            redisTemplate.delete(rateLimitKey)
            throw exception
        }
    }

    @Transactional
    fun checkAuthCode(request: VerifyCode): EmailVerificationResponse {
        val email = request.email.trim().lowercase()
        val user =
            userRepository.findByEmailIgnoreCase(email)
                ?: throw BusinessException(AuthErrorCode.USER_NOT_FOUND)

        when (user.status) {
            UserStatus.ACTIVE -> throw BusinessException(AuthErrorCode.ALREADY_VERIFIED)
            UserStatus.LOCKED -> throw BusinessException(AuthErrorCode.ACCOUNT_LOCKED)
            UserStatus.UNVERIFIED -> Unit
        }

        val codeKey = "$CODE_KEY_PREFIX$email"
        val savedCode =
            redisTemplate.opsForValue().get(codeKey)
                ?: throw BusinessException(AuthErrorCode.VERIFY_CODE_EXPIRED)

        if (savedCode != request.code) {
            throw BusinessException(AuthErrorCode.VERIFY_CODE_INVALID)
        }

        user.verifyEmail()
        redisTemplate.delete(codeKey)
        redisTemplate.delete("$RATE_LIMIT_KEY_PREFIX$email")

        return EmailVerificationResponse.from(user)
    }

    private fun generateVerificationCode(): String =
        secureRandom.nextInt(VERIFICATION_CODE_BOUND).toString().padStart(VERIFICATION_CODE_LENGTH, '0')

    private companion object {
        const val VERIFY_EMAIL_TEMPLATE_PATH = "templates/email/verify-email.html"
        const val CODE_KEY_PREFIX = "auth:email-verification:code:"
        const val RATE_LIMIT_KEY_PREFIX = "auth:email-verification:rate:"
        const val VERIFICATION_CODE_LENGTH = 6
        const val VERIFICATION_CODE_BOUND = 1_000_000
        val secureRandom = SecureRandom()
        val VERIFY_CODE_TTL: Duration = Duration.ofMinutes(5)
        val RATE_LIMIT_TTL: Duration = Duration.ofMinutes(1)
    }
}
