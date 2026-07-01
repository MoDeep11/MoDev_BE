package modeep.modev.domain.project.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import modeep.modev.domain.project.controller.dto.request.SaveProjectRequest
import modeep.modev.domain.project.controller.dto.request.UpdateProjectMetadataRequest
import modeep.modev.global.response.ApiResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@Tag(name = "Project", description = "프로젝트 관리 API")
interface ProjectControllerDocs {
    @Operation(
        summary = "프로젝트 목록 조회",
        description = "저장된 프로젝트 목록을 페이지 단위로 조회한다. keyword가 있으면 프로젝트명 또는 기술 스택으로 검색한다.",
        operationId = "getProjects",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ApiResponse::class))],
            ),
        ],
    )
    fun getProjects(
        @Parameter(description = "페이지 번호", example = "1")
        @RequestParam(name = "page", defaultValue = "1") page: Int,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(name = "size", defaultValue = "20") size: Int,
        @Parameter(description = "프로젝트명 또는 기술 스택 검색 키워드", example = "spring")
        @RequestParam(name = "keyword", required = false) keyword: String?,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "프로젝트 저장",
        description = "프로젝트명, 설명, 선택한 분야/기술 스택/의존성을 저장한다.",
        operationId = "saveProject",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "프로젝트 저장 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ApiResponse::class),
                        examples = [
                            ExampleObject(
                                value =
                                    """
                                    {
                                      "success": true,
                                      "data": {
                                        "projectId": "550e8400-e29b-41d4-a716-446655440000"
                                      },
                                      "error": null
                                    }
                                    """,
                            ),
                        ],
                    ),
                ],
            ),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
        ],
    )
    fun saveProject(
        @Valid @RequestBody request: SaveProjectRequest,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "프로젝트 상세 조회",
        description = "프로젝트 ID로 저장된 프로젝트 상세 정보와 파일 트리를 조회한다.",
        operationId = "getProject",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "상세 조회 성공"),
            SwaggerApiResponse(responseCode = "404", description = "프로젝트 없음"),
        ],
    )
    fun getProjectDetail(
        @Parameter(description = "프로젝트 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "프로젝트 메타데이터 수정",
        description = "프로젝트명과 설명을 수정한다.",
        operationId = "updateProjectMetadata",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "수정 성공"),
            SwaggerApiResponse(responseCode = "400", description = "입력값 유효성 오류"),
            SwaggerApiResponse(responseCode = "404", description = "프로젝트 없음"),
        ],
    )
    fun updateProjectMetadata(
        @Parameter(description = "프로젝트 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
        @Valid @RequestBody request: UpdateProjectMetadataRequest,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "프로젝트 삭제 (소프트 삭제)",
        description = "프로젝트를 소프트 삭제하고 영구 삭제 예정 일시를 반환한다.",
        operationId = "deleteProject",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "삭제 성공"),
            SwaggerApiResponse(responseCode = "404", description = "프로젝트 없음"),
        ],
    )
    fun deleteProject(
        @Parameter(description = "프로젝트 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
    ): ResponseEntity<ApiResponse>
}
