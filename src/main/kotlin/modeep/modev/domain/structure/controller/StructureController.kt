package modeep.modev.domain.structure.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import modeep.modev.domain.structure.controller.dto.request.GenerateStructureRequest
import modeep.modev.domain.structure.service.DownloadStructureService
import modeep.modev.domain.structure.service.GenerateStructureService
import modeep.modev.domain.structure.service.GetStructureFileService
import modeep.modev.domain.structure.service.GetStructureStatusService
import modeep.modev.domain.structure.service.StreamStructureService
import modeep.modev.global.response.ApiResponse
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@RestController
@RequestMapping("/projects/structures")
class StructureController(
    private val generateStructureService: GenerateStructureService,
    private val getStructureStatusService: GetStructureStatusService,
    private val streamStructureService: StreamStructureService,
    private val getStructureFileService: GetStructureFileService,
    private val downloadStructureService: DownloadStructureService,
) : StructureControllerDocs {
    @PostMapping
    override fun generate(
        @RequestBody request: GenerateStructureRequest,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .accepted()
            .body(
                ApiResponse(
                    success = true,
                    data = generateStructureService.execute(request),
                ),
            )

    @GetMapping("/{projectId}")
    override fun getStatus(
        @PathVariable projectId: UUID,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = getStructureStatusService.execute(projectId),
            ),
        )

    @GetMapping(
        "/{projectId}/stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    override fun stream(
        @PathVariable projectId: UUID,
        response: HttpServletResponse,
    ): ResponseEntity<SseEmitter> {
        response.setHeader("X-Accel-Buffering", "no")

        return ResponseEntity.ok(streamStructureService.connect(projectId))
    }

    @GetMapping("/{projectId}/files")
    override fun getFile(
        @PathVariable projectId: UUID,
        @RequestParam(name = "filePath") path: String,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                data = getStructureFileService.execute(projectId, path),
            ),
        )

    @PostMapping("/{projectId}/download")
    override fun issueDownloadUrl(
        @PathVariable projectId: UUID,
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity.ok(
            ApiResponse(
                success = true,
                data =
                    downloadStructureService.issueDirectDownloadUrl(
                        projectId = projectId,
                        downloadUrl = request.requestURL.toString(),
                    ),
            ),
        )

    @GetMapping("/{projectId}/download")
    fun downloadZip(
        @PathVariable projectId: UUID,
    ): ResponseEntity<ByteArray> {
        val zip = downloadStructureService.createDownloadZip(projectId)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename(zip.fileName)
                    .build()
                    .toString(),
            )
            .body(zip.content)
    }
}
