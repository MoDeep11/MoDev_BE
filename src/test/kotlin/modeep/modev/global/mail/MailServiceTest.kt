package modeep.modev.global.mail

import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import modeep.modev.global.config.properties.MailProperties
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.mail.javamail.JavaMailSender
import java.util.Properties
import kotlin.test.assertEquals

class MailServiceTest {
    private val javaMailSender = mock(JavaMailSender::class.java)
    private val service =
        MailService(
            javaMailSender = javaMailSender,
            mailProperties =
                MailProperties(
                    address = "noreply@modev.dev",
                    name = "MODEV",
                ),
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
}
