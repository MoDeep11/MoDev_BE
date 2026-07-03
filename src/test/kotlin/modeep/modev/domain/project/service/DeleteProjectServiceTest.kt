package modeep.modev.domain.project.service

import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class DeleteProjectServiceTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var service: DeleteProjectService

    @BeforeEach
    fun setUp() {
        projectRepository = mock(ProjectRepository::class.java)
        service = DeleteProjectService(projectRepository)
    }

    @Test
    fun `soft deletes project and returns hard delete schedule`() {
        val projectId = UUID.randomUUID()
        val project = Project(id = projectId, projectName = "project")
        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(project)

        val response = service.deleteProject(projectId)

        assertEquals(projectId, response.projectId)
        assertNotNull(project.deletedAt)
        assertEquals(project.deletedAt, response.deletedAt)
        assertEquals(30, ChronoUnit.DAYS.between(response.deletedAt, response.hardDeleteScheduledAt))
        verify(projectRepository).findByIdAndDeletedAtIsNull(projectId)
    }

    @Test
    fun `throws when deleting missing project`() {
        val projectId = UUID.randomUUID()
        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(null)

        val exception =
            assertFailsWith<BusinessException> {
                service.deleteProject(projectId)
            }

        assertEquals(ProjectErrorCode.PROJECT_NOT_FOUND, exception.errorCode)
    }
}
