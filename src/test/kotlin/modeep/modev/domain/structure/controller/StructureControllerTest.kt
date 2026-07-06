package modeep.modev.domain.structure.controller

import modeep.modev.domain.project.entity.ProjectStatus
import modeep.modev.domain.structure.controller.dto.request.GenerateStructureRequest
import modeep.modev.domain.structure.controller.dto.response.DownloadStructureResponse
import modeep.modev.domain.structure.controller.dto.response.GenerateStructureResponse
import modeep.modev.domain.structure.service.DownloadStructureService
import modeep.modev.domain.structure.service.GenerateStructureService
import modeep.modev.domain.structure.service.GetStructureFileService
import modeep.modev.domain.structure.service.GetStructureStatusService
import modeep.modev.domain.structure.service.StreamStructureService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StructureControllerTest {
    private val generateStructureService = mock(GenerateStructureService::class.java)
    private val streamStructureService = mock(StreamStructureService::class.java)
    private val downloadStructureService = mock(DownloadStructureService::class.java)
    private val controller =
        StructureController(
            generateStructureService = generateStructureService,
            getStructureStatusService = mock(GetStructureStatusService::class.java),
            streamStructureService = streamStructureService,
            getStructureFileService = mock(GetStructureFileService::class.java),
            downloadStructureService = downloadStructureService,
        )

    @Test
    fun `accepts project structure generation request`() {
        val projectId = UUID.randomUUID()
        val request = GenerateStructureRequest(projectId)
        val serviceResponse = GenerateStructureResponse(projectId, ProjectStatus.PENDING)
        `when`(generateStructureService.execute(request)).thenReturn(serviceResponse)

        val response = controller.generate(request)

        assertEquals(HttpStatus.ACCEPTED, response.statusCode)
        assertTrue(response.body!!.success)
        assertEquals(serviceResponse, response.body!!.data)
        assertNull(response.body!!.error)
        verify(generateStructureService).execute(request)
    }

    @Test
    fun `connects project structure stream with proxy buffering disabled`() {
        val projectId = UUID.randomUUID()
        val servletResponse = MockHttpServletResponse()
        val emitter = SseEmitter()
        `when`(streamStructureService.connect(projectId)).thenReturn(emitter)

        val response = controller.stream(projectId, servletResponse)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("no", servletResponse.getHeader("X-Accel-Buffering"))
        assertEquals(emitter, response.body)
        verify(streamStructureService).connect(projectId)
    }

    @Test
    fun `issues existing project zip download url`() {
        val projectId = UUID.randomUUID()
        val serviceResponse =
            DownloadStructureResponse(
                downloadUrl = "https://storage.example.com/my-project.zip?token=abc",
                expiresAt = OffsetDateTime.parse("2025-05-26T12:00:00Z"),
                fileName = "my-project_20250526.zip",
            )
        `when`(downloadStructureService.issueDownloadUrl(projectId)).thenReturn(serviceResponse)

        val response = controller.issueDownloadUrl(projectId)

        assertTrue(response.body!!.success)
        assertEquals(serviceResponse, response.body!!.data)
        assertNull(response.body!!.error)
        verify(downloadStructureService).issueDownloadUrl(projectId)
    }
}
