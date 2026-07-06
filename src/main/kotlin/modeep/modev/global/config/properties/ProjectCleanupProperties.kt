package modeep.modev.global.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "project.cleanup.anonymous")
data class ProjectCleanupProperties(
    val cron: String = "0 0 0 * * *",
    val zone: String = "Asia/Seoul",
    val retentionDays: Long = 3,
)
