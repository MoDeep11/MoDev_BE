package modeep.modev.domain.project.service

import modeep.modev.domain.project.controller.dto.request.UpdateProjectMetadataRequest
import modeep.modev.domain.project.entity.Project
import modeep.modev.domain.project.repository.ProjectRepository
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.ProjectErrorCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID
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
    fun `updates project metadata and normalizes blank description`() {
        val projectId = UUID.randomUUID()
        val project = Project(id = projectId, projectName = "old", description = "old description")
        val request = UpdateProjectMetadataRequest(projectName = "new", description = "   ")
        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(project)

        val response = service.updateProjectMetadata(projectId, request)

        assertEquals(projectId, response.projectId)
        assertEquals("new", response.projectName)
        assertNull(response.description)
        assertEquals("new", project.projectName)
        assertNull(project.description)
        verify(projectRepository).findByIdAndDeletedAtIsNull(projectId)
    }

    @Test
    fun `throws when updating metadata for missing project`() {
        val projectId = UUID.randomUUID()
        val request = UpdateProjectMetadataRequest(projectName = "new", description = "description")
        `when`(projectRepository.findByIdAndDeletedAtIsNull(projectId)).thenReturn(null)

        val exception =
            assertFailsWith<BusinessException> {
                service.updateProjectMetadata(projectId, request)
            }

        assertEquals(ProjectErrorCode.PROJECT_NOT_FOUND, exception.errorCode)
    }
}
