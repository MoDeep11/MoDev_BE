package modeep.modev.domain.catalog.registry.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import modeep.modev.global.config.properties.CatalogRegistryProperties
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.RegistryErrorCode
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Duration

abstract class RegistryClientSupport(
    private val objectMapper: ObjectMapper,
    private val properties: CatalogRegistryProperties,
) {
    protected fun getJson(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): JsonNode {
        val body = getBody(url, headers)

        return runCatching {
            objectMapper.readTree(body)
        }.getOrElse {
            throw BusinessException(
                errorCode = RegistryErrorCode.RESPONSE_PARSE_FAILED,
                message = it.message,
                cause = it,
            )
        }
    }

    protected fun getText(url: String): String = getBody(url)

    private fun getBody(
        url: String,
        headers: Map<String, String> = emptyMap(),
    ): String {
        val request =
            headers.entries.fold(WebClient.builder().build().get().uri(url)) { spec, (name, value) ->
                spec.header(name, value)
            }

        return runCatching {
            request
                .retrieve()
                .bodyToMono<String>()
                .block(Duration.ofMillis(properties.timeoutMillis))
        }.getOrElse {
            throw BusinessException(
                errorCode = RegistryErrorCode.REQUEST_FAILED,
                message = it.message,
                cause = it,
            )
        } ?: throw BusinessException(RegistryErrorCode.EMPTY_RESPONSE)
    }

    protected fun invalidIdentifier(message: String) {
        throw BusinessException(
            errorCode = RegistryErrorCode.INVALID_IDENTIFIER,
            message = message,
        )
    }
}
