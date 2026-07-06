package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.SendEmailRequest
import modeep.modev.domain.auth.service.VerifyEmailService.Companion.VERIFIED_KEY_PREFIX
import modeep.modev.global.mail.EmailTemplateRenderer
import modeep.modev.global.mail.MailMessage
import modeep.modev.global.mail.MailService
import modeep.modev.global.mail.MailSubjects
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Duration

@Service
class SendEmailService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val mailService: MailService,
    private val emailTemplateRenderer: EmailTemplateRenderer,
) {
    fun execute(request: SendEmailRequest) {
        val email = request.email.trim().lowercase()

        val code = generateVerificationCode()
        val codeKey = "$CODE_KEY_PREFIX$email"
        val verifiedKey = "$VERIFIED_KEY_PREFIX$email"

        try {
            redisTemplate.delete(verifiedKey)
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
                    to = request.email,
                    subject = MailSubjects.VERIFY_EMAIL,
                    body = body,
                    isHtml = true,
                ),
            )
        } catch (exception: Exception) {
            redisTemplate.delete(codeKey)
            throw exception
        }
    }

    private fun generateVerificationCode(): String =
        secureRandom.nextInt(VERIFICATION_CODE_BOUND).toString().padStart(VERIFICATION_CODE_LENGTH, '0')

    private companion object {
        const val VERIFY_EMAIL_TEMPLATE_PATH = "templates/email/verify-email.html"
        const val CODE_KEY_PREFIX = "auth:email-verification:code:"
        const val VERIFICATION_CODE_LENGTH = 6
        const val VERIFICATION_CODE_BOUND = 1_000_000
        val secureRandom = SecureRandom()
        val VERIFY_CODE_TTL: Duration = Duration.ofMinutes(5)
    }
}
