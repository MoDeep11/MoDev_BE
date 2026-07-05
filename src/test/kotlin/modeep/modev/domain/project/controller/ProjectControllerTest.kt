package modeep.modev.domain.project.controller

import modeep.modev.domain.project.controller.dto.request.UpdateProjectStacksRequest
import modeep.modev.domain.project.controller.dto.response.UpdateProjectStacksResponse
import modeep.modev.domain.project.service.DeleteProjectService
import modeep.modev.domain.project.service.GetProjectService
import modeep.modev.domain.project.service.PatchProjectService
import modeep.modev.domain.project.service.PostProjectService
import modeep.modev.domain.project.service.UpdateProjectStacksService
import modeep.modev.domain.structure.controller.dto.response.DownloadStructureResponse
import modeep.modev.domain.structure.service.DownloadStructureService
import modeep.modev.global.security.jwt.JwtPrincipal
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProjectControllerTest {
    private val user = JwtPrincipal(userId = "1", status = "ACTIVE")
    private val userId = 1L
    private val updateProjectStacksService = mock(UpdateProjectStacksService::class.java)
    private val downloadStructureService = mock(DownloadStructureService::class.java)
    private val controller =
        ProjectController(
            getProjectService = mock(GetProjectService::class.java),
            patchProjectService = mock(PatchProjectService::class.java),
            deleteProjectService = mock(DeleteProjectService::class.java),
            postProjectService = mock(PostProjectService::class.java),
            updateProjectStacksService = updateProjectStacksService,
            downloadStructureService = downloadStructureService,
        )

    @Test
    fun `updates project stacks and returns accepted regeneration request`() {
        val projectId = UUID.randomUUID()
        val request =
            UpdateProjectStacksRequest(
                fieldIds = listOf("domain_fe", "domain_be"),
                stackIds = listOf("stack_spring", "stack_react", "stack_redis"),
                dependencyIds = listOf("dep_spring_security", "dep_jpa"),
            )
        val serviceResponse = UpdateProjectStacksResponse(projectId, "PENDING")
        `when`(updateProjectStacksService.execute(projectId, userId, request)).thenReturn(serviceResponse)

        val response = controller.updateProjectStacks(user, projectId, request)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertTrue(response.body!!.success)
        assertEquals(serviceResponse, response.body!!.data)
        assertNull(response.body!!.error)
        verify(updateProjectStacksService).execute(projectId, userId, request)
    }

    @Test
    fun `issues project download url`() {
        val projectId = UUID.randomUUID()
        val serviceResponse =
            DownloadStructureResponse(
                downloadUrl = "https://storage.example.com/my-project.zip?token=abc",
                expiresAt = OffsetDateTime.parse("2025-05-26T12:00:00Z"),
                fileName = "my-project_20250526.zip",
            )
        `when`(downloadStructureService.issueDownloadUrl(projectId, userId)).thenReturn(serviceResponse)

        val response = controller.issueDownloadUrl(user, projectId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body!!.success)
        assertEquals(serviceResponse, response.body!!.data)
        assertNull(response.body!!.error)
        verify(downloadStructureService).issueDownloadUrl(projectId, userId)
    }
}
