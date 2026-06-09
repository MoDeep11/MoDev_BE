package modeep.modev.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "stream.structure")
data class StructureStreamProperties(
    val timeoutMillis: Long,
)
