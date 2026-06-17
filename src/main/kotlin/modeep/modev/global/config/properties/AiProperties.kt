package modeep.modev.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai.server")
data class AiProperties(
    val baseUrl: String,
    val internalApiKey: String,
    val timeout: Timeout,
) {
    data class Timeout(
        val connect: Long,
        val read: Long,
    )
}
