package modeep.modev.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "catalog.registry.sync")
data class CatalogRegistryProperties(
    val enabled: Boolean = true,
    val cron: String = "0 0 3 * * *",
    val timeoutMillis: Long = 5000,
    val githubToken: String? = null,
)
