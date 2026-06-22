package modeep.modev.global.mail

import jakarta.mail.internet.InternetAddress
import modeep.modev.global.config.properties.MailProperties
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class MailService(
    private val javaMailSender: JavaMailSender,
    private val mailProperties: MailProperties,
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
}
