package modeep.modev.domain.project.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import modeep.modev.domain.project.service.ProjectCleanupService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ProjectCleanupScheduler(
    private val projectCleanupService: ProjectCleanupService,
) {
    @Scheduled(
        cron = "\${project.cleanup.anonymous.cron:0 0 0 * * *}",
        zone = "\${project.cleanup.anonymous.zone:Asia/Seoul}",
    )
    fun deleteExpiredAnonymousProjects() {
        val deletedCount = projectCleanupService.deleteExpiredAnonymousProjects()
        logger.info { "Expired anonymous project cleanup finished. deleted=$deletedCount" }
    }
}
