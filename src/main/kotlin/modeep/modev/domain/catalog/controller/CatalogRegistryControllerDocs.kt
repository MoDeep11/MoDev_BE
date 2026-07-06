package modeep.modev.domain.catalog.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import modeep.modev.domain.catalog.registry.vo.CatalogRegistryTargetType
import modeep.modev.global.response.ApiResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@Tag(name = "Catalog Registry", description = "카탈로그 외부 레지스트리 동기화 API")
interface CatalogRegistryControllerDocs {
    @Operation(
        summary = "레지스트리 버전 목록 조회",
        description = "targetType과 publicId에 해당하는 외부 레지스트리 버전 목록을 조회한다.",
        operationId = "getCatalogRegistryVersions",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "버전 목록 조회 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ApiResponse::class))],
            ),
        ],
    )
    fun getVersions(
        @Parameter(description = "레지스트리 동기화 대상 타입", example = "DEPENDENCY")
        @RequestParam targetType: CatalogRegistryTargetType,
        @Parameter(description = "카탈로그 publicId", example = "spring-boot-starter-web")
        @RequestParam publicId: String,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "레지스트리 버전 동기화",
        description = "targetType과 publicId에 해당하는 카탈로그 항목의 외부 레지스트리 버전을 동기화한다.",
        operationId = "syncCatalogRegistry",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "동기화 성공"),
        ],
    )
    fun sync(
        @Parameter(description = "레지스트리 동기화 대상 타입", example = "DEPENDENCY")
        @RequestParam targetType: CatalogRegistryTargetType,
        @Parameter(description = "카탈로그 publicId", example = "spring-boot-starter-web")
        @RequestParam publicId: String,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "전체 레지스트리 버전 동기화",
        description = "동기화 대상 전체의 외부 레지스트리 버전을 동기화한다.",
        operationId = "syncAllCatalogRegistry",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "전체 동기화 성공"),
        ],
    )
    fun syncAll(): ResponseEntity<ApiResponse>
}
