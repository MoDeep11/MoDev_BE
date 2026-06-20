package modeep.modev.global.mail

import jakarta.mail.internet.InternetAddress
import modeep.modev.domain.auth.controller.dto.request.EmailVerificationSendRequest
import modeep.modev.domain.auth.controller.dto.request.VerifyCode
import modeep.modev.domain.auth.controller.dto.response.EmailVerificationResponse
import modeep.modev.domain.auth.entity.UserStatus
import modeep.modev.domain.auth.repository.UserRepository
import modeep.modev.global.config.properties.MailProperties
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Duration

@Component
class MailService(
    private val javaMailSender: JavaMailSender,
    private val mailProperties: MailProperties,
    private val userRepository: UserRepository,
    private val redisTemplate: StringRedisTemplate,
    private val emailTemplateRenderer: EmailTemplateRenderer,
) {
    fun send(message: MailMessage) {
        val mimeMessage = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, false, Charsets.UTF_8.name())

        helper.setFrom(InternetAddress(mailProperties.address, mailProperties.name))
        helper.setTo(message.to)
        helper.setSubject(message.subject)
        helper.setText(message.body, message.isHtml)

        if (message.cc.isNotEmpty()) {
            helper.setCc(message.cc.toTypedArray())
        }
        if (message.bcc.isNotEmpty()) {
            helper.setBcc(message.bcc.toTypedArray())
        }

        javaMailSender.send(mimeMessage)
    }

    @Transactional(readOnly = true)
    fun sendVerificationCode(request: EmailVerificationSendRequest) {
        val email = request.email.trim().lowercase()
        val user =
            userRepository.findByEmailIgnoreCase(email)
                ?: throw BusinessException(AuthErrorCode.USER_NOT_FOUND)

        if (user.status == UserStatus.ACTIVE) {
            throw BusinessException(AuthErrorCode.ALREADY_VERIFIED)
        }

        val rateLimitKey = "$RATE_LIMIT_KEY_PREFIX$email"
        if (redisTemplate.hasKey(rateLimitKey)) {
            throw BusinessException(AuthErrorCode.RESEND_RATE_LIMIT)
        }

        val code = generateVerificationCode()
        val codeKey = "$CODE_KEY_PREFIX$email"
        redisTemplate.opsForValue().set(codeKey, code, VERIFY_CODE_TTL)

        try {
            val body =
                emailTemplateRenderer.render(
                    templatePath = VERIFY_EMAIL_TEMPLATE_PATH,
                    variables =
                        code.mapIndexed { index, digit ->
                            "CODE_${index + 1}" to digit.toString()
                        }.toMap(),
                )

            send(
                MailMessage(
                    to = user.email,
                    subject = MailSubjects.VERIFY_EMAIL,
                    body = body,
                    isHtml = true,
                ),
            )
        } catch (exception: RuntimeException) {
            redisTemplate.delete(codeKey)
            throw exception
        }

        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_TTL)
    }

    @Transactional
    fun checkAuthCode(request: VerifyCode): EmailVerificationResponse {
        val email = request.email.trim().lowercase()
        val user =
            userRepository.findByEmailIgnoreCase(email)
                ?: throw BusinessException(AuthErrorCode.USER_NOT_FOUND)

        if (user.status == UserStatus.ACTIVE) {
            throw BusinessException(AuthErrorCode.ALREADY_VERIFIED)
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
