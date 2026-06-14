package modeep.modev.domain.project.controller

import jakarta.validation.Valid
import modeep.modev.domain.project.controller.dto.request.SaveProjectRequest
import modeep.modev.domain.project.controller.dto.request.UpdateProjectMetadataRequest
import modeep.modev.domain.project.service.DeleteProjectService
import modeep.modev.domain.project.service.GetProjectService
import modeep.modev.domain.project.service.PatchProjectService
import modeep.modev.domain.project.service.PostProjectService
import modeep.modev.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val getProjectService: GetProjectService,
    private val patchProjectService: PatchProjectService,
    private val deleteProjectService: DeleteProjectService,
    private val postProjectService: PostProjectService,
) {
    @GetMapping
    fun getProjects(
        @RequestParam(name = "page", defaultValue = "1") page: Int,
        @RequestParam(name = "size", defaultValue = "20") size: Int,
        @RequestParam(name = "keyword", required = false) keyword: String?,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = getProjectService.getProjects(page, size, keyword),
                    error = null,
                ),
            )

    @PostMapping
    fun saveProject(
        @Valid @RequestBody request: SaveProjectRequest,
    ): ResponseEntity<ApiResponse> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as? Long
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = postProjectService.saveProject(request, userId),
                    error = null,
                ),
            )
    }

    @GetMapping("/{projectId}")
    fun getProjectDetail(
        @PathVariable projectId: String,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = getProjectService.getProjectDetail(projectId),
                    error = null,
                ),
            )

    @PatchMapping("/{projectId}/metadata")
    fun updateProjectMetadata(
        @PathVariable projectId: String,
        @Valid @RequestBody request: UpdateProjectMetadataRequest,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = patchProjectService.updateProjectMetadata(projectId, request),
                    error = null,
                ),
            )

    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @PathVariable projectId: String,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = deleteProjectService.deleteProject(projectId),
                    error = null,
                ),
            )
}
