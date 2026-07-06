package modeep.modev.domain.project.service

import modeep.modev.domain.project.repository.ProjectDependencyRepository
import modeep.modev.domain.project.repository.ProjectFieldRepository
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.project.repository.ProjectTechStackRepository
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.config.properties.ProjectCleanupProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ProjectCleanupService(
    private val projectRepository: ProjectRepository,
    private val projectFieldRepository: ProjectFieldRepository,
    private val projectTechStackRepository: ProjectTechStackRepository,
    private val projectDependencyRepository: ProjectDependencyRepository,
    private val structureFileRepository: StructureFileRepository,
    private val properties: ProjectCleanupProperties,
) {
    @Transactional
    fun deleteExpiredAnonymousProjects(now: Instant = Instant.now()): Int {
        val cutoff = now.minus(properties.retentionDays, ChronoUnit.DAYS)
        val projects = projectRepository.findAllByUserIdIsNullAndDeletedAtLessThanEqual(cutoff)
        if (projects.isEmpty()) {
            return 0
        }

        val projectIds = projects.mapNotNull { it.id }

        projectIds.forEach { projectId ->
            projectDependencyRepository.deleteAllByIdProjectId(projectId)
            projectTechStackRepository.deleteAllByIdProjectId(projectId)
            projectFieldRepository.deleteAllByIdProjectId(projectId)
            structureFileRepository.deleteAllByProjectId(projectId)
        }

        projectRepository.deleteAllInBatch(projects)

        return projects.size
    }
}
