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
class GoProxyRegistryClient(
    objectMapper: ObjectMapper,
    properties: CatalogRegistryProperties,
) : RegistryClientSupport(objectMapper, properties),
    RegistryClient {
    override fun supports(registryType: RegistryType): Boolean = registryType == RegistryType.GO_PROXY

    override fun fetchVersions(identifier: String): RegistryVersionResult {
        val encodedIdentifier = UriUtils.encodePath(identifier, StandardCharsets.UTF_8)
        val versions =
            getText("https://proxy.golang.org/$encodedIdentifier/@v/list")
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toList()
                .asReversed()

        return RegistryVersionResult(
            latestVersion = RegistryVersionSelector.latestStable(versions),
            versions = versions,
        )
    }
}
