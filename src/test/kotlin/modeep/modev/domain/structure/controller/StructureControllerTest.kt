package modeep.modev.domain.structure.controller

import modeep.modev.domain.structure.controller.dto.response.DownloadStructureResponse
import modeep.modev.domain.structure.service.DownloadStructureService
import modeep.modev.domain.structure.service.GenerateStructureService
import modeep.modev.domain.structure.service.GetStructureFileService
import modeep.modev.domain.structure.service.GetStructureStatusService
import modeep.modev.domain.structure.service.StreamStructureService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StructureControllerTest {
    private val downloadStructureService = mock(DownloadStructureService::class.java)
    private val controller =
        StructureController(
            generateStructureService = mock(GenerateStructureService::class.java),
            getStructureStatusService = mock(GetStructureStatusService::class.java),
            streamStructureService = mock(StreamStructureService::class.java),
            getStructureFileService = mock(GetStructureFileService::class.java),
            downloadStructureService = downloadStructureService,
        )

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
