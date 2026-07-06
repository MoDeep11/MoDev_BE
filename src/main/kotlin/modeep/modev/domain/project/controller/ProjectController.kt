package modeep.modev.domain.project.controller

import jakarta.validation.Valid
import modeep.modev.domain.project.controller.dto.request.SaveProjectRequest
import modeep.modev.domain.project.controller.dto.request.UpdateProjectMetadataRequest
import modeep.modev.domain.project.controller.dto.request.UpdateProjectStacksRequest
import modeep.modev.domain.project.service.DeleteProjectService
import modeep.modev.domain.project.service.GetProjectService
import modeep.modev.domain.project.service.PatchProjectService
import modeep.modev.domain.project.service.PostProjectService
import modeep.modev.domain.project.service.UpdateProjectStacksService
import modeep.modev.domain.structure.service.DownloadStructureService
import modeep.modev.global.response.ApiResponse
import modeep.modev.global.security.jwt.JwtPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val getProjectService: GetProjectService,
    private val patchProjectService: PatchProjectService,
    private val deleteProjectService: DeleteProjectService,
    private val postProjectService: PostProjectService,
    private val updateProjectStacksService: UpdateProjectStacksService,
    private val downloadStructureService: DownloadStructureService,
) : ProjectControllerDocs {
    @GetMapping
    override fun getProjects(
        @AuthenticationPrincipal user: JwtPrincipal,
        @RequestParam(name = "page", defaultValue = "1") page: Int,
        @RequestParam(name = "size", defaultValue = "20") size: Int,
        @RequestParam(name = "keyword", required = false) keyword: String?,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = getProjectService.getProjects(page, size, keyword, user.userId.toLong()),
                    error = null,
                ),
            )

    @PostMapping
    override fun saveProject(
        @AuthenticationPrincipal user: JwtPrincipal?,
        @Valid @RequestBody request: SaveProjectRequest,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = postProjectService.saveProject(request, user?.userId?.toLong()),
                    error = null,
                ),
            )

    @GetMapping("/{projectId}")
    override fun getProjectDetail(
        @AuthenticationPrincipal user: JwtPrincipal,
        @PathVariable projectId: UUID,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = getProjectService.getProjectDetail(projectId, user.userId.toLong()),
                    error = null,
                ),
            )

    @PatchMapping("/{projectId}/metadata")
    override fun updateProjectMetadata(
        @AuthenticationPrincipal user: JwtPrincipal,
        @PathVariable projectId: UUID,
        @Valid @RequestBody request: UpdateProjectMetadataRequest,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = patchProjectService.updateProjectMetadata(projectId, user.userId.toLong(), request),
                    error = null,
                ),
            )

    @PatchMapping("/{projectId}/stacks")
    override fun updateProjectStacks(
        @AuthenticationPrincipal user: JwtPrincipal,
        @PathVariable projectId: UUID,
        @Valid @RequestBody request: UpdateProjectStacksRequest,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(
                ApiResponse(
                    success = true,
                    data = updateProjectStacksService.execute(projectId, user.userId.toLong(), request),
                    error = null,
                ),
            )

    @PostMapping("/{projectId}/download")
    override fun issueDownloadUrl(
        @AuthenticationPrincipal user: JwtPrincipal,
        @PathVariable projectId: UUID,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = downloadStructureService.issueDownloadUrl(projectId, user.userId.toLong()),
                    error = null,
                ),
            )

    @DeleteMapping("/{projectId}")
    override fun deleteProject(
        @AuthenticationPrincipal user: JwtPrincipal,
        @PathVariable projectId: UUID,
    ): ResponseEntity<ApiResponse> =
        ResponseEntity
            .status(HttpStatus.OK)
            .body(
                ApiResponse(
                    success = true,
                    data = deleteProjectService.deleteProject(projectId, user.userId.toLong()),
                    error = null,
                ),
            )
}
