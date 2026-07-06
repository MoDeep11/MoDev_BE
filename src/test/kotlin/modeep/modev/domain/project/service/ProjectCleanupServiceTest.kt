package modeep.modev.domain.project.service

import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.repository.ProjectDependencyRepository
import modeep.modev.domain.project.repository.ProjectFieldRepository
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.domain.project.repository.ProjectTechStackRepository
import modeep.modev.domain.structure.repository.StructureFileRepository
import modeep.modev.global.config.properties.ProjectCleanupProperties
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectCleanupServiceTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var projectFieldRepository: ProjectFieldRepository
    private lateinit var projectTechStackRepository: ProjectTechStackRepository
    private lateinit var projectDependencyRepository: ProjectDependencyRepository
    private lateinit var structureFileRepository: StructureFileRepository
    private lateinit var service: ProjectCleanupService

    @BeforeEach
    fun setUp() {
        projectRepository = mock(ProjectRepository::class.java)
        projectFieldRepository = mock(ProjectFieldRepository::class.java)
        projectTechStackRepository = mock(ProjectTechStackRepository::class.java)
        projectDependencyRepository = mock(ProjectDependencyRepository::class.java)
        structureFileRepository = mock(StructureFileRepository::class.java)
        service =
            ProjectCleanupService(
                projectRepository = projectRepository,
                projectFieldRepository = projectFieldRepository,
                projectTechStackRepository = projectTechStackRepository,
                projectDependencyRepository = projectDependencyRepository,
                structureFileRepository = structureFileRepository,
                properties = ProjectCleanupProperties(retentionDays = 3),
            )
    }

    @Test
    fun `deletes anonymous projects deleted more than three days ago`() {
        val now = Instant.parse("2026-07-05T15:00:00Z")
        val cutoff = Instant.parse("2026-07-02T15:00:00Z")
        val firstProjectId = UUID.randomUUID()
        val secondProjectId = UUID.randomUUID()
        val projects =
            listOf(
                Project(id = firstProjectId, projectName = "first", deletedAt = cutoff),
                Project(id = secondProjectId, projectName = "second", deletedAt = cutoff.minusSeconds(1)),
            )

        `when`(projectRepository.findAllByUserIdIsNullAndDeletedAtLessThanEqual(cutoff)).thenReturn(projects)

        val deletedCount = service.deleteExpiredAnonymousProjects(now)

        assertEquals(2, deletedCount)
        listOf(firstProjectId, secondProjectId).forEach { projectId ->
            verify(projectDependencyRepository).deleteAllByIdProjectId(projectId)
            verify(projectTechStackRepository).deleteAllByIdProjectId(projectId)
            verify(projectFieldRepository).deleteAllByIdProjectId(projectId)
            verify(structureFileRepository).deleteAllByProjectId(projectId)
        }
        verify(projectRepository).deleteAllInBatch(projects)
    }

    @Test
    fun `does nothing when expired anonymous projects do not exist`() {
        val now = Instant.parse("2026-07-05T15:00:00Z")
        val cutoff = Instant.parse("2026-07-02T15:00:00Z")

        `when`(projectRepository.findAllByUserIdIsNullAndDeletedAtLessThanEqual(cutoff)).thenReturn(emptyList())

        val deletedCount = service.deleteExpiredAnonymousProjects(now)

        assertEquals(0, deletedCount)
        verify(projectRepository, never()).deleteAllInBatch(emptyList<Project>())
        verifyNoInteractions(
            projectFieldRepository,
            projectTechStackRepository,
            projectDependencyRepository,
            structureFileRepository,
        )
    }

    @Test
    fun `uses configured retention days to find expired anonymous projects`() {
        val now = Instant.parse("2026-07-05T15:00:00Z")
        val cutoff = Instant.parse("2026-06-28T15:00:00Z")
        val service =
            ProjectCleanupService(
                projectRepository = projectRepository,
                projectFieldRepository = projectFieldRepository,
                projectTechStackRepository = projectTechStackRepository,
                projectDependencyRepository = projectDependencyRepository,
                structureFileRepository = structureFileRepository,
                properties = ProjectCleanupProperties(retentionDays = 7),
            )

        `when`(projectRepository.findAllByUserIdIsNullAndDeletedAtLessThanEqual(cutoff)).thenReturn(emptyList())

        val deletedCount = service.deleteExpiredAnonymousProjects(now)

        assertEquals(0, deletedCount)
        verify(projectRepository).findAllByUserIdIsNullAndDeletedAtLessThanEqual(cutoff)
    }
}
