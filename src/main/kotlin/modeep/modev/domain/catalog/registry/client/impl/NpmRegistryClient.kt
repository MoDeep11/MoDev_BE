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
class NpmRegistryClient(
    objectMapper: ObjectMapper,
    properties: CatalogRegistryProperties,
) : RegistryClientSupport(objectMapper, properties),
    RegistryClient {
    override fun supports(registryType: RegistryType): Boolean = registryType == RegistryType.NPM

    override fun fetchVersions(identifier: String): RegistryVersionResult {
        val encodedIdentifier = UriUtils.encodePathSegment(identifier, StandardCharsets.UTF_8)
        val json = getJson("https://registry.npmjs.org/$encodedIdentifier")
        val versions = json.path("versions").fieldNames().asSequence().toList()

        return RegistryVersionResult(
            latestVersion =
                json.path("dist-tags").path("latest")
                    .takeIf { it.isTextual }
                    ?.asText()
                    ?.takeIf { RegistryVersionSelector.isStable(it) }
                    ?: RegistryVersionSelector.latestStable(versions),
            versions = versions,
        )
    }
}
