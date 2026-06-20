package modeep.modev.domain.catalog.registry.client.impl

import com.fasterxml.jackson.databind.ObjectMapper
import modeep.modev.domain.catalog.entity.vo.RegistryType
import modeep.modev.domain.catalog.registry.client.RegistryClient
import modeep.modev.domain.catalog.registry.client.RegistryClientSupport
import modeep.modev.domain.catalog.registry.client.response.RegistryVersionResult
import modeep.modev.domain.catalog.registry.util.RegistryVersionSelector
import modeep.modev.global.config.properties.CatalogRegistryProperties
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class MavenCentralRegistryClient(
    objectMapper: ObjectMapper,
    properties: CatalogRegistryProperties,
) : RegistryClientSupport(objectMapper, properties),
    RegistryClient {
    override fun supports(registryType: RegistryType): Boolean = registryType == RegistryType.MAVEN_CENTRAL

    override fun fetchVersions(identifier: String): RegistryVersionResult {
        val (groupId, artifactId) = parseIdentifier(identifier)
        val url =
            UriComponentsBuilder
                .fromUriString("https://search.maven.org/solrsearch/select")
                .queryParam("q", "g:$groupId AND a:$artifactId")
                .queryParam("core", "gav")
                .queryParam("rows", 20)
                .queryParam("wt", "json")
                .build()
                .encode()
                .toUriString()

        val versions =
            getJson(url)
                .path("response")
                .path("docs")
                .mapNotNull { it.path("v").takeIf { version -> version.isTextual }?.asText() }

        return RegistryVersionResult(
            latestVersion = RegistryVersionSelector.latestStable(versions),
            versions = versions,
        )
    }

    private fun parseIdentifier(identifier: String): Pair<String, String> {
        val parts = identifier.split(":", limit = 2)
        if (parts.size != 2 || parts.any { it.isBlank() }) {
            invalidIdentifier("Maven identifier must be formatted as groupId:artifactId")
        }
        return parts[0] to parts[1]
    }
}
