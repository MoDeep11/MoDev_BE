package modeep.modev.domain.project.service

import modeep.modev.domain.project.controller.dto.request.UpdateProjectMetadataRequest
import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PatchProjectServiceTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var service: PatchProjectService

    @BeforeEach
    fun setUp() {
        projectRepository = mock(ProjectRepository::class.java)
        service = PatchProjectService(projectRepository)
    }

    @Test
    fun `updates project metadata and trims blank description to null`() {
        val projectId = UUID.randomUUID()
        val project = Project(id = projectId, projectName = "old project", description = "old description")
        val request = UpdateProjectMetadataRequest(projectName = "new project", description = "   ")

        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(project)

        val response = service.updateProjectMetadata(projectId, request)

        assertEquals(projectId, response.projectId)
        assertEquals("new project", response.projectName)
        assertNull(response.description)
        assertEquals("new project", project.projectName)
        assertNull(project.description)
    }

    @Test
    fun `throws project not found when active project does not exist`() {
        val projectId = UUID.randomUUID()
        val request = UpdateProjectMetadataRequest(projectName = "new project", description = null)

        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(null)

        val exception =
            assertFailsWith<BusinessException> {
                service.updateProjectMetadata(projectId, request)
            }

        assertEquals(ProjectErrorCode.PROJECT_NOT_FOUND, exception.errorCode)
    }
}
