package modeep.modev.domain.catalog.registry.client.impl

import com.fasterxml.jackson.databind.ObjectMapper
import modeep.modev.domain.catalog.entity.vo.RegistryType
import modeep.modev.domain.catalog.registry.client.RegistryClient
import modeep.modev.domain.catalog.registry.client.RegistryClientSupport
import modeep.modev.domain.catalog.registry.client.response.RegistryVersionResult
import modeep.modev.domain.catalog.registry.util.RegistryVersionSelector
import modeep.modev.global.config.properties.CatalogRegistryProperties
import org.springframework.stereotype.Component
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets

@Component
class DockerHubRegistryClient(
    objectMapper: ObjectMapper,
    properties: CatalogRegistryProperties,
) : RegistryClientSupport(objectMapper, properties),
    RegistryClient {
    private companion object {
        const val PAGE_SIZE = 100
    }

    override fun supports(registryType: RegistryType): Boolean = registryType == RegistryType.DOCKER_HUB

    override fun fetchVersions(identifier: String): RegistryVersionResult {
        val (namespace, image) = parseIdentifier(identifier)
        val encodedNamespace = UriUtils.encodePathSegment(namespace, StandardCharsets.UTF_8)
        val encodedImage = UriUtils.encodePathSegment(image, StandardCharsets.UTF_8)
        val versions = mutableListOf<String>()
        var nextUrl: String? =
            "https://hub.docker.com/v2/repositories/$encodedNamespace/$encodedImage/tags?page_size=$PAGE_SIZE"

        while (nextUrl != null) {
            val json = getJson(nextUrl)
            val pageVersions =
                json.path("results")
                    .mapNotNull { it.path("name").takeIf { version -> version.isTextual }?.asText() }

            versions.addAll(pageVersions)
            nextUrl =
                json.path("next")
                    .takeIf { it.isTextual && it.asText().isNotBlank() }
                    ?.asText()
        }

        return RegistryVersionResult(
            latestVersion = RegistryVersionSelector.latestStable(versions),
            versions = versions,
        )
    }

    private fun parseIdentifier(identifier: String): Pair<String, String> {
        val parts = identifier.split("/", limit = 2)
        if (parts.size != 2 || parts.any { it.isBlank() }) {
            invalidIdentifier("Docker Hub identifier must be formatted as namespace/image")
        }
        return parts[0] to parts[1]
    }
}
