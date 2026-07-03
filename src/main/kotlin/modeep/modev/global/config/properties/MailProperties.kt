package modeep.modev.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.mail.sender")
data class MailProperties(
    val address: String,
    val name: String = "MoDev",
)
