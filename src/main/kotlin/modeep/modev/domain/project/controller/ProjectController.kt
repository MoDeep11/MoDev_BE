package modeep.modev.domain.project.controller

import jakarta.validation.Valid
import modeep.modev.domain.project.controller.dto.request.SaveProjectRequest
import modeep.modev.domain.project.controller.dto.request.UpdateProjectMetadataRequest
import modeep.modev.domain.project.service.ProjectService
import modeep.modev.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    private val projectService: ProjectService,
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
                    data = projectService.getProjects(page, size, keyword),
                    error = null,
                ),
            )

    @PostMapping
    fun saveProject(
        @Valid @RequestBody request: SaveProjectRequest,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse(
                    success = true,
                    data = projectService.saveProject(request),
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
                    data = projectService.updateProjectMetadata(projectId, request),
                    error = null,
                ),
            )
}
