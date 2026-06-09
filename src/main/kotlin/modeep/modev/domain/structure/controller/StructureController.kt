package modeep.modev.domain.structure.controller

import jakarta.servlet.http.HttpServletResponse
import modeep.modev.domain.structure.service.StreamStructureService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@RestController
@RequestMapping("/projects/structures")
class StructureController(
    private val streamStructureService: StreamStructureService,
) {
    @GetMapping(
        "/{projectId}/stream",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE],
    )
    fun stream(
        @PathVariable projectId: UUID,
        response: HttpServletResponse,
    ): SseEmitter {
        response.setHeader("X-Accel-Buffering", "no")

        return streamStructureService.connect(projectId.toString())
    }
}
