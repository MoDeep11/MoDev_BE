package modeep.modev.global.config

import io.netty.channel.ChannelOption
import modeep.modev.global.config.properties.AiProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebClientConfig {
    @Bean
    fun externalWebClient(aiProperties: AiProperties): WebClient {
        val httpClient =
            HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, aiProperties.timeout.connect.toInt())
                .responseTimeout(Duration.ofMillis(aiProperties.timeout.read))

        return WebClient.builder()
            .baseUrl(aiProperties.baseUrl)
            .defaultHeader("X-Internal-API-Key", aiProperties.internalApiKey)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}
