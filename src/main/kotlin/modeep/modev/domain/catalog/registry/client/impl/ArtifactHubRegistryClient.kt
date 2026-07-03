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
class ArtifactHubRegistryClient(
    objectMapper: ObjectMapper,
    properties: CatalogRegistryProperties,
) : RegistryClientSupport(objectMapper, properties),
    RegistryClient {
    override fun supports(registryType: RegistryType): Boolean = registryType == RegistryType.ARTIFACT_HUB

    override fun fetchVersions(identifier: String): RegistryVersionResult {
        val (repo, chart) = parseIdentifier(identifier)
        val encodedRepo = UriUtils.encodePathSegment(repo, StandardCharsets.UTF_8)
        val encodedChart = UriUtils.encodePathSegment(chart, StandardCharsets.UTF_8)
        val versions =
            getJson("https://artifacthub.io/api/v1/packages/helm/$encodedRepo/$encodedChart")
                .path("available_versions")
                .mapNotNull { it.path("version").takeIf { version -> version.isTextual }?.asText() }

        return RegistryVersionResult(
            latestVersion = RegistryVersionSelector.latestStable(versions),
            versions = versions,
        )
    }

    private fun parseIdentifier(identifier: String): Pair<String, String> {
        val parts = identifier.split("/", limit = 2)
        if (parts.size != 2 || parts.any { it.isBlank() }) {
            invalidIdentifier("Artifact Hub identifier must be formatted as repo/chart")
        }
        return parts[0] to parts[1]
    }
}
