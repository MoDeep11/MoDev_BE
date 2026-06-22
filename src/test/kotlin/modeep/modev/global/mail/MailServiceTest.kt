package modeep.modev.global.mail

import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import modeep.modev.domain.auth.controller.dto.request.EmailVerificationSendRequest
import modeep.modev.domain.auth.controller.dto.request.VerifyCode
import modeep.modev.domain.user.entity.User
import modeep.modev.domain.user.entity.UserStatus
import modeep.modev.domain.user.repository.UserRepository
import modeep.modev.global.config.properties.MailProperties
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.mail.javamail.JavaMailSender
import java.time.Duration
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MailServiceTest {
    private val javaMailSender = mock(JavaMailSender::class.java)
    private val userRepository = mock(UserRepository::class.java)
    private val redisTemplate = mock(StringRedisTemplate::class.java)
    private val emailTemplateRenderer = EmailTemplateRenderer()
    private val service =
        MailService(
            javaMailSender = javaMailSender,
            mailProperties =
                MailProperties(
                    address = "noreply@modev.dev",
                    name = "MODEV",
                ),
            userRepository = userRepository,
            redisTemplate = redisTemplate,
            emailTemplateRenderer = emailTemplateRenderer,
        )

    @Test
    fun `sends plain text mail`() {
        `when`(javaMailSender.createMimeMessage())
            .thenReturn(MimeMessage(Session.getInstance(Properties())))
        val messageCaptor = ArgumentCaptor.forClass(MimeMessage::class.java)

        service.send(
            MailMessage(
                to = "user@example.com",
                subject = "Verify your email",
                body = "verification-code",
            ),
        )

        verify(javaMailSender).send(messageCaptor.capture())

        val sentMessage = messageCaptor.value
        assertEquals("MODEV <noreply@modev.dev>", sentMessage.from.single().toString())
        assertEquals("user@example.com", sentMessage.getRecipients(Message.RecipientType.TO).single().toString())
        assertEquals("Verify your email", sentMessage.subject)
        assertEquals("verification-code", sentMessage.content.toString())
    }

    @Test
    fun `sets cc and bcc recipients`() {
        `when`(javaMailSender.createMimeMessage())
            .thenReturn(MimeMessage(Session.getInstance(Properties())))
        val messageCaptor = ArgumentCaptor.forClass(MimeMessage::class.java)

        service.send(
            MailMessage(
                to = "user@example.com",
                subject = "Notice",
                body = "<strong>Hello</strong>",
                isHtml = true,
                cc = listOf("cc@example.com"),
                bcc = listOf("bcc@example.com"),
            ),
        )

        verify(javaMailSender).send(messageCaptor.capture())

        val sentMessage = messageCaptor.value
        assertEquals("cc@example.com", sentMessage.getRecipients(Message.RecipientType.CC).single().toString())
        assertEquals("bcc@example.com", sentMessage.getRecipients(Message.RecipientType.BCC).single().toString())
    }

    @Test
    fun `sends verification code and stores six digit string`() {
        @Suppress("UNCHECKED_CAST")
        val valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
        val user =
            User(
                id = 1L,
                email = "user@example.com",
                passwordHash = "encoded-password",
                status = UserStatus.UNVERIFIED,
            )
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(user)
        `when`(redisTemplate.hasKey("auth:email-verification:rate:user@example.com")).thenReturn(false)
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
        `when`(javaMailSender.createMimeMessage())
            .thenReturn(MimeMessage(Session.getInstance(Properties())))
        val messageCaptor = ArgumentCaptor.forClass(MimeMessage::class.java)

        service.sendVerificationCode(EmailVerificationSendRequest(" User@Example.com "))

        val codeCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(valueOperations).set(
            eq("auth:email-verification:code:user@example.com"),
            codeCaptor.capture(),
            eq(Duration.ofMinutes(5)),
        )
        verify(javaMailSender).send(messageCaptor.capture())
        verify(valueOperations).set(
            "auth:email-verification:rate:user@example.com",
            "1",
            Duration.ofMinutes(1),
        )

        val code = codeCaptor.value
        val content = messageCaptor.value.content.toString()
        assertTrue(code.matches(Regex("\\d{6}")))
        code.forEachIndexed { index, digit ->
            assertTrue(content.contains(digit.toString()), "CODE_${index + 1} was not rendered")
        }
        assertFalse(content.contains(Regex("\\{\\{CODE_[1-6]}}")))
    }

    @Test
    fun `verifies code and activates user`() {
        @Suppress("UNCHECKED_CAST")
        val valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
        val user =
            User(
                id = 1L,
                email = "user@example.com",
                passwordHash = "encoded-password",
                status = UserStatus.UNVERIFIED,
            )
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(user)
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
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
        @Suppress("UNCHECKED_CAST")
        val valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
        val user =
            User(
                id = 1L,
                email = "user@example.com",
                passwordHash = "encoded-password",
                status = UserStatus.UNVERIFIED,
            )
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(user)
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
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
        @Suppress("UNCHECKED_CAST")
        val valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
        val user =
            User(
                id = 1L,
                email = "user@example.com",
                passwordHash = "encoded-password",
                status = UserStatus.UNVERIFIED,
            )
        `when`(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(user)
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
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
}
