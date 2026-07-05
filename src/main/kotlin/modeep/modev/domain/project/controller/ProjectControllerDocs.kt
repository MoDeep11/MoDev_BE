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
import modeep.modev.domain.project.controller.dto.request.UpdateProjectStacksRequest
import modeep.modev.global.response.ApiResponse
import modeep.modev.global.security.jwt.JwtPrincipal
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
        @AuthenticationPrincipal user: JwtPrincipal,
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
        @AuthenticationPrincipal user: JwtPrincipal?,
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
        @AuthenticationPrincipal user: JwtPrincipal,
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
        @AuthenticationPrincipal user: JwtPrincipal,
        @Parameter(description = "프로젝트 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
        @Valid @RequestBody request: UpdateProjectMetadataRequest,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "프로젝트 스택 수정",
        description =
            """
            프로젝트의 분야, 기술 스택, 의존성 구성을 수정하고 프로젝트 구조 재생성 요청을 접수한다.
            응답으로 받은 status가 PENDING이면 구조 생성 상태 조회 또는 SSE 스트리밍으로 진행 상태를 확인한다.
            """,
        operationId = "updateProjectStacks",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "202",
                description = "스택 수정 및 재생성 요청 접수",
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
                                        "projectId": "550e8400-e29b-41d4-a716-446655440000",
                                        "status": "PENDING"
                                      },
                                      "error": null
                                    }
                                    """,
                            ),
                        ],
                    ),
                ],
            ),
            SwaggerApiResponse(responseCode = "400", description = "입력값 유효성 오류"),
            SwaggerApiResponse(responseCode = "404", description = "프로젝트 없음"),
        ],
    )
    fun updateProjectStacks(
        @AuthenticationPrincipal user: JwtPrincipal,
        @Parameter(description = "프로젝트 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
        @Valid @RequestBody request: UpdateProjectStacksRequest,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "프로젝트 zip 다운로드 URL 발급",
        description =
            """
            저장된 프로젝트 전체를 zip으로 압축한 후 다운로드 URL을 반환한다.
            URL은 presigned 방식으로 만료 시간이 있으며, 직접 URL 노출을 방지한다. (@NFR-SEC-03)

            ### 파일명 규칙
            `{projectName}_{YYYYMMDD}.zip`
            프로젝트명에 특수문자가 포함된 경우 파일 시스템 안전 문자로 치환한다.
            """,
        operationId = "downloadProject",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "다운로드 URL 발급 성공",
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
                                        "downloadUrl": "https://storage.example.com/my-project_20250526.zip?token=abc...",
                                        "expiresAt": "2025-05-26T11:00:00Z",
                                        "fileName": "my-project_20250526.zip"
                                      },
                                      "error": null
                                    }
                                    """,
                            ),
                        ],
                    ),
                ],
            ),
            SwaggerApiResponse(responseCode = "404", description = "프로젝트 없음"),
            SwaggerApiResponse(responseCode = "500", description = "서버 내부 오류"),
        ],
    )
    fun issueDownloadUrl(
        @AuthenticationPrincipal user: JwtPrincipal,
        @Parameter(description = "프로젝트 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
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
        @AuthenticationPrincipal user: JwtPrincipal,
        @Parameter(description = "프로젝트 식별자", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
    ): ResponseEntity<ApiResponse>
}
