package modeep.modev.domain.project.service

import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DeleteProjectServiceTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var service: DeleteProjectService

    @BeforeEach
    fun setUp() {
        projectRepository = mock(ProjectRepository::class.java)
        service = DeleteProjectService(projectRepository)
    }

    @Test
    fun `marks project as deleted and returns hard delete schedule`() {
        val projectId = UUID.randomUUID()
        val project = Project(id = projectId, projectName = "project")
        val before = Instant.now()

        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(project)

        val response = service.deleteProject(projectId)

        val after = Instant.now()
        val deletedAt = assertNotNull(project.deletedAt)
        assertEquals(projectId, response.projectId)
        assertEquals(deletedAt, response.deletedAt)
        assertEquals(Duration.ofDays(30), Duration.between(response.deletedAt, response.hardDeleteScheduledAt))
        assertTrue(!response.deletedAt.isBefore(before))
        assertTrue(!response.deletedAt.isAfter(after))
    }

    @Test
    fun `throws project not found when active project does not exist`() {
        val projectId = UUID.randomUUID()

        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(null)

        val exception =
            assertFailsWith<BusinessException> {
                service.deleteProject(projectId)
            }

        assertEquals(ProjectErrorCode.PROJECT_NOT_FOUND, exception.errorCode)
    }
}
