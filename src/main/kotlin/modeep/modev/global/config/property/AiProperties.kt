package modeep.modev.global.config.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai.server")
data class AiProperties(
    val baseUrl: String,
    val internalApiKey: String,
)
