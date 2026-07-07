package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.SendEmailRequest
import modeep.modev.global.config.properties.MailProperties
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.mail.EmailTemplateRenderer
import modeep.modev.global.mail.MailMessage
import modeep.modev.global.mail.MailService
import modeep.modev.global.mail.MailSubjects
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.mail.javamail.JavaMailSender
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SendEmailServiceTest {
    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var valueOperations: ValueOperations<String, String>
    private lateinit var emailTemplateRenderer: EmailTemplateRenderer
    private lateinit var mailService: RecordingMailService
    private lateinit var service: SendEmailService

    @BeforeEach
    fun setUp() {
        @Suppress("UNCHECKED_CAST")
        redisTemplate = mock(RedisTemplate::class.java) as RedisTemplate<String, String>
        @Suppress("UNCHECKED_CAST")
        valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
        emailTemplateRenderer = EmailTemplateRenderer()
        mailService = RecordingMailService()
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
        service =
            SendEmailService(
                redisTemplate = redisTemplate,
                mailService = mailService,
                emailTemplateRenderer = emailTemplateRenderer,
            )
    }

    @Test
    fun `stores verification code and sends rendered mail`() {
        `when`(valueOperations.get("auth:email-verification:verified:user@example.com")).thenReturn(null)

        service.execute(SendEmailRequest(" User@Example.com "))

        val codeCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(valueOperations).set(
            eq("auth:email-verification:code:user@example.com"),
            codeCaptor.capture(),
            eq(Duration.ofMinutes(5)),
        )
        val code = codeCaptor.value
        assertTrue(code.matches(Regex("\\d{6}")))

        val sentMessage = requireNotNull(mailService.sentMessage)
        assertEquals("user@example.com", sentMessage.to)
        assertEquals(MailSubjects.VERIFY_EMAIL, sentMessage.subject)
        assertTrue(sentMessage.isHtml)
        assertEquals(
            emailTemplateRenderer.render(
                templatePath = "templates/email/verify-email.html",
                variables =
                    code.mapIndexed { index, digit ->
                        "CODE_${index + 1}" to digit.toString()
                    }.toMap(),
            ),
            sentMessage.body,
        )
    }

    @Test
    fun `rejects sending verification code for already verified email`() {
        `when`(valueOperations.get("auth:email-verification:verified:user@example.com")).thenReturn("true")

        val exception =
            assertFailsWith<BusinessException> {
                service.execute(SendEmailRequest("user@example.com"))
            }

        assertEquals(AuthErrorCode.ALREADY_VERIFIED, exception.errorCode)
        verify(valueOperations, never()).set(
            eq("auth:email-verification:code:user@example.com"),
            org.mockito.ArgumentMatchers.anyString(),
            eq(Duration.ofMinutes(5)),
        )
    }

    private class RecordingMailService :
        MailService(
            javaMailSender = mock(JavaMailSender::class.java),
            mailProperties = MailProperties(address = "noreply@modev.dev", name = "MODEV"),
        ) {
        var sentMessage: MailMessage? = null

        override fun send(message: MailMessage) {
            sentMessage = message
        }
    }
}
