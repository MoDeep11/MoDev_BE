package modeep.modev.global.config

import modeep.modev.global.config.properties.AiProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    fun externalWebClient(aiProperties: AiProperties): WebClient {
        return WebClient.builder()
            .baseUrl(aiProperties.baseUrl)
            .defaultHeader("X-Internal-API-Key", aiProperties.internalApiKey)
            .build()
    }
}
