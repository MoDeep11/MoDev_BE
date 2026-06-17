package modeep.modev.domain.structure.controller

import jakarta.servlet.http.HttpServletResponse
import modeep.modev.domain.structure.controller.dto.request.GenerateStructureRequest
import modeep.modev.domain.structure.service.DownloadStructureService
import modeep.modev.domain.structure.service.GenerateStructureService
import modeep.modev.domain.structure.service.GetStructureFileService
import modeep.modev.domain.structure.service.GetStructureStatusService
import modeep.modev.domain.structure.service.StreamStructureService
import modeep.modev.global.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
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

    override fun getStatus(
        @PathVariable projectId: UUID,
    ): ApiResponse =
        ApiResponse(
            success = true,
            data = getStructureStatusService.execute(projectId),
        )

    override fun stream(
        @PathVariable projectId: UUID,
        response: HttpServletResponse,
    ): SseEmitter {
        response.setHeader("X-Accel-Buffering", "no")

        return streamStructureService.connect(projectId.toString())
    }

    override fun getFile(
        @PathVariable projectId: UUID,
        @RequestParam(name = "filePath") path: String,
    ): ApiResponse =
        ApiResponse(
            success = true,
            data = getStructureFileService.execute(projectId, path),
        )

    override fun issueDownloadUrl(
        @PathVariable projectId: UUID,
    ): ApiResponse =
        ApiResponse(
            success = true,
            data = downloadStructureService.issueDownloadUrl(projectId),
        )
}
