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
class GithubReleasesRegistryClient(
    objectMapper: ObjectMapper,
    private val properties: CatalogRegistryProperties,
) : RegistryClientSupport(objectMapper, properties),
    RegistryClient {
    override fun supports(registryType: RegistryType): Boolean = registryType == RegistryType.GITHUB_RELEASES

    override fun fetchVersions(identifier: String): RegistryVersionResult {
        val (owner, repo) = parseIdentifier(identifier)
        val encodedOwner = UriUtils.encodePathSegment(owner, StandardCharsets.UTF_8)
        val encodedRepo = UriUtils.encodePathSegment(repo, StandardCharsets.UTF_8)
        val headers =
            properties.githubToken
                ?.takeIf { it.isNotBlank() }
                ?.let { mapOf("Authorization" to "Bearer $it") }
                ?: emptyMap()
        val versions =
            getJson(
                url = "https://api.github.com/repos/$encodedOwner/$encodedRepo/releases?per_page=20",
                headers = headers,
            ).filter { !it.path("prerelease").asBoolean(false) }
                .mapNotNull { it.path("tag_name").takeIf { version -> version.isTextual }?.asText() }

        return RegistryVersionResult(
            latestVersion = RegistryVersionSelector.latestStable(versions),
            versions = versions,
        )
    }

    private fun parseIdentifier(identifier: String): Pair<String, String> {
        val parts = identifier.split("/", limit = 2)
        if (parts.size != 2 || parts.any { it.isBlank() }) {
            invalidIdentifier("GitHub Releases identifier must be formatted as owner/repo")
        }
        return parts[0] to parts[1]
    }
}
