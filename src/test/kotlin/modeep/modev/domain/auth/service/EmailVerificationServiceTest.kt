package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.EmailVerificationSendRequest
import modeep.modev.domain.auth.controller.dto.request.VerifyCode
import modeep.modev.domain.user.entity.User
import modeep.modev.domain.user.entity.UserStatus
import modeep.modev.domain.user.repository.UserRepository
import modeep.modev.global.config.properties.MailProperties
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.mail.EmailTemplateRenderer
import modeep.modev.global.mail.MailMessage
import modeep.modev.global.mail.MailService
import modeep.modev.global.mail.MailSubjects
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EmailVerificationServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var valueOperations: ValueOperations<String, String>
    private lateinit var emailTemplateRenderer: EmailTemplateRenderer
    private lateinit var mailService: RecordingMailService
    private lateinit var service: EmailVerificationService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        @Suppress("UNCHECKED_CAST")
        redisTemplate = mock(RedisTemplate::class.java) as RedisTemplate<String, String>
        @Suppress("UNCHECKED_CAST")
        valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
        emailTemplateRenderer = EmailTemplateRenderer()
        mailService = RecordingMailService()
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)

        service =
            EmailVerificationService(
                userRepository = userRepository,
                redisTemplate = redisTemplate,
                emailTemplateRenderer = emailTemplateRenderer,
                mailService = mailService,
            )
    }

    @Test
    fun `stores verification code and sends rendered mail`() {
        val user = unverifiedUser()
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(user)
        `when`(
            valueOperations.setIfAbsent(
                "auth:email-verification:rate:user@example.com",
                "1",
                Duration.ofMinutes(1),
            ),
        ).thenReturn(true)
        service.sendVerificationCode(EmailVerificationSendRequest(" User@Example.com "))

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
    fun `rejects verification code resend during rate limit`() {
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(unverifiedUser())
        `when`(
            valueOperations.setIfAbsent(
                "auth:email-verification:rate:user@example.com",
                "1",
                Duration.ofMinutes(1),
            ),
        ).thenReturn(false)

        val exception =
            assertFailsWith<BusinessException> {
                service.sendVerificationCode(EmailVerificationSendRequest("user@example.com"))
            }

        assertEquals(AuthErrorCode.RESEND_RATE_LIMIT, exception.errorCode)
        assertEquals(null, mailService.sentMessage)
    }

    @Test
    fun `verifies code and activates user`() {
        val user = unverifiedUser()
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(user)
        `when`(valueOperations.get("auth:email-verification:code:user@example.com")).thenReturn("012345")

        val response =
            service.checkAuthCode(
                VerifyCode(
                    email = " User@Example.com ",
                    code = "012345",
                ),
            )

        assertEquals("user_1", response.userId)
        assertEquals("ACTIVE", response.status)
        assertEquals(UserStatus.ACTIVE, user.status)
        verify(redisTemplate).delete("auth:email-verification:code:user@example.com")
        verify(redisTemplate).delete("auth:email-verification:rate:user@example.com")
    }

    @Test
    fun `rejects invalid verification code without deleting saved code`() {
        val user = unverifiedUser()
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(user)
        `when`(valueOperations.get("auth:email-verification:code:user@example.com")).thenReturn("012345")

        val exception =
            assertFailsWith<BusinessException> {
                service.checkAuthCode(
                    VerifyCode(
                        email = "user@example.com",
                        code = "999999",
                    ),
                )
            }

        assertEquals(AuthErrorCode.VERIFY_CODE_INVALID, exception.errorCode)
        assertEquals(UserStatus.UNVERIFIED, user.status)
        verify(redisTemplate, never()).delete("auth:email-verification:code:user@example.com")
    }

    @Test
    fun `rejects expired verification code`() {
        val user = unverifiedUser()
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(user)
        `when`(valueOperations.get("auth:email-verification:code:user@example.com")).thenReturn(null)

        val exception =
            assertFailsWith<BusinessException> {
                service.checkAuthCode(
                    VerifyCode(
                        email = "user@example.com",
                        code = "012345",
                    ),
                )
            }

        assertEquals(AuthErrorCode.VERIFY_CODE_EXPIRED, exception.errorCode)
        assertEquals(UserStatus.UNVERIFIED, user.status)
    }

    private fun unverifiedUser() =
        User(
            id = 1L,
            email = "user@example.com",
            passwordHash = "encoded-password",
            status = UserStatus.UNVERIFIED,
        )

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
