package modeep.modev.domain.catalog.registry.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import modeep.modev.domain.catalog.registry.service.CatalogRegistrySyncAllService
import modeep.modev.global.config.properties.CatalogRegistryProperties
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class CatalogRegistryScheduler(
    private val properties: CatalogRegistryProperties,
    private val catalogRegistrySyncAllService: CatalogRegistrySyncAllService,
) {
    @Scheduled(cron = "\${catalog.registry.sync.cron:0 0 3 * * *}")
    fun syncCatalogRegistryVersions() {
        if (!properties.enabled) {
            return
        }

        val result = catalogRegistrySyncAllService.syncAll()
        logger.info {
            "Catalog registry sync finished. synced=${result.syncedCount}, failed=${result.failedCount}"
        }
    }
}
