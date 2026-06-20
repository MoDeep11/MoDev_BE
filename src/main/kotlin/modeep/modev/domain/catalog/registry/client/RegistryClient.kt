package modeep.modev.domain.catalog.registry.client

import modeep.modev.domain.catalog.entity.vo.RegistryType
import modeep.modev.domain.catalog.registry.response.RegistryVersionResult

interface RegistryClient {
    fun supports(registryType: RegistryType): Boolean

    fun fetchVersions(identifier: String): RegistryVersionResult
}
