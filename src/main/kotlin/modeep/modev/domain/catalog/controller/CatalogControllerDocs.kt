package modeep.modev.domain.catalog.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import modeep.modev.global.response.ApiResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestParam
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@Tag(name = "Catalog", description = "프로젝트 구성 메타데이터 API")
interface CatalogControllerDocs {
    @Operation(
        summary = "개발 분야 목록 조회",
        description = "프로젝트 생성 시 선택할 수 있는 개발 분야 목록을 반환한다.",
        operationId = "getFields",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "분야 목록 조회 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ApiResponse::class))],
            ),
        ],
    )
    fun getFields(): ApiResponse

    @Operation(
        summary = "기술 스택 목록 조회",
        description = "선택한 분야 ID 목록을 기준으로 기술 스택 목록을 조회한다. keyword가 있으면 스택명으로 필터링한다.",
        operationId = "getStacks",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "스택 목록 조회 성공",
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
                                        "stacks": []
                                      },
                                      "error": null
                                    }
                                    """,
                            ),
                        ],
                    ),
                ],
            ),
            SwaggerApiResponse(responseCode = "400", description = "필수 파라미터 누락"),
        ],
    )
    fun getTechStacks(
        @Parameter(description = "분야 ID 목록 (콤마 구분)", example = "backend,frontend")
        @RequestParam fieldIds: String?,
        @Parameter(description = "스택명 검색 키워드", example = "spring")
        @RequestParam(required = false) keyword: String?,
    ): ApiResponse

    @Operation(
        summary = "의존성 목록 조회",
        description = "선택한 스택 ID 목록을 기준으로 의존성 목록을 조회한다. keyword가 있으면 의존성명으로 필터링한다.",
        operationId = "getDependencies",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "의존성 목록 조회 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ApiResponse::class))],
            ),
            SwaggerApiResponse(responseCode = "400", description = "필수 파라미터 누락"),
        ],
    )
    fun getDependencies(
        @Parameter(description = "스택 ID 목록 (콤마 구분)", example = "spring-boot,react")
        @RequestParam stackIds: String?,
        @Parameter(description = "의존성명 검색 키워드", example = "security")
        @RequestParam(required = false) keyword: String?,
    ): ApiResponse
}
