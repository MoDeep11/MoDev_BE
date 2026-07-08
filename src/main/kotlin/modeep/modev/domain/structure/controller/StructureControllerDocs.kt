package modeep.modev.domain.structure.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import modeep.modev.domain.structure.controller.dto.request.GenerateStructureRequest
import modeep.modev.global.response.ApiResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@Tag(name = "Generate", description = "프로젝트 구조 AI 생성 API")
interface StructureControllerDocs {
    @Operation(
        summary = "프로젝트 구조 AI 생성 요청",
        description =
            """
            선택된 스택과 의존성을 기반으로 AI가 프로젝트 구조와 기초 설정 파일을 생성한다.
            생성은 비동기로 처리되며, 응답으로 받은 `projectId`로 SSE 스트리밍에 연결한다.
            비로그인 사용자도 요청 가능하다.

            ### 생성 결과물 예시
            - 디렉토리: `/frontend`, `/backend`, `/docker`, `/docs`
            - 파일: `README.md`, `.gitignore`, `docker-compose.yml`, `.env.example`, `build.gradle` 등
            """,
        operationId = "generateProject",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "202",
                description = "생성 요청 접수 (비동기 처리)",
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
            SwaggerApiResponse(
                responseCode = "404",
                description = "프로젝트를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ApiResponse::class),
                        examples = [
                            ExampleObject(
                                value =
                                    """
                                    {
                                      "success": false,
                                      "data": null,
                                      "error": {
                                        "code": "PROJECT_NOT_FOUND",
                                        "message": "해당 ID의 프로젝트를 찾을 수 없습니다."
                                      }
                                    }
                                    """,
                            ),
                        ],
                    ),
                ],
            ),
            SwaggerApiResponse(responseCode = "500", description = "서버 내부 오류"),
        ],
    )
    fun generate(
        @RequestBody request: GenerateStructureRequest,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "프로젝트 구조 생성 상태 조회",
        description =
            """
            생성 요청의 현재 상태와 완료 시 파일 트리 결과를 반환한다.
            완료 결과의 `fileTree`는 `file_created` 이벤트로 저장된 파일/디렉토리 목록을 기준으로 재구성한다.
            `complete` 이벤트 payload는 생성 완료 알림 및 요약 정보로만 사용하며, 파일 트리의 source of truth로 사용하지 않는다.
            비로그인 사용자도 접근 가능하다.

            ### status 값
            | 값 | 설명 |
            |---|---|
            | `PENDING` | 생성 대기 중 |
            | `GENERATING` | 생성 중 |
            | `COMPLETED` | 생성 완료 |
            | `FAILED` | 생성 실패 |
            """,
        operationId = "getGenerateStatus",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "상태 조회 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = ApiResponse::class),
                        examples = [
                            ExampleObject(
                                name = "pending",
                                summary = "생성 대기 중",
                                value =
                                    """
                                    {
                                      "success": true,
                                      "data": {
                                        "projectId": "550e8400-e29b-41d4-a716-446655440000",
                                        "status": "PENDING",
                                        "result": null
                                      },
                                      "error": null
                                    }
                                    """,
                            ),
                            ExampleObject(
                                name = "completed",
                                summary = "생성 완료",
                                value =
                                    """
                                    {
                                      "success": true,
                                      "data": {
                                        "projectId": "550e8400-e29b-41d4-a716-446655440000",
                                        "status": "COMPLETED",
                                        "result": {
                                          "fileTree": [
                                            {
                                              "name": "backend",
                                              "type": "DIRECTORY",
                                              "children": [
                                                {
                                                  "name": "src",
                                                  "type": "DIRECTORY",
                                                  "children": []
                                                }
                                              ]
                                            },
                                            {
                                              "name": "README.md",
                                              "type": "FILE",
                                              "children": []
                                            }
                                          ]
                                        }
                                      },
                                      "error": null
                                    }
                                    """,
                            ),
                            ExampleObject(
                                name = "failed",
                                summary = "생성 실패",
                                value =
                                    """
                                    {
                                      "success": true,
                                      "data": {
                                        "projectId": "550e8400-e29b-41d4-a716-446655440000",
                                        "status": "FAILED",
                                        "result": null
                                      },
                                      "error": null
                                    }
                                    """,
                            ),
                        ],
                    ),
                ],
            ),
            SwaggerApiResponse(responseCode = "404", description = "프로젝트 ID 없음 또는 만료"),
        ],
    )
    fun getStatus(
        @Parameter(description = "프로젝트 생성 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "SSE 스트리밍 연결",
        description =
            """
            생성 진행 상태를 SSE(Server-Sent Events)로 실시간 수신한다.
            클라이언트는 이벤트를 순차적으로 수신하여 파일 트리를 점진적으로 렌더링한다.
            비로그인 사용자도 접근 가능하다.

            ### 이벤트 타입
            | 이벤트 | 설명 |
            |---|---|
            | `connected` | SSE 연결 수립 완료 |
            | `progress` | 생성 진행 상태 안내 메시지 |
            | `file_created` | 파일 또는 디렉토리 생성 완료 |
            | `heartbeat` | SSE 연결 유지를 위한 주기 신호 |
            | `complete` | 전체 생성 완료 |
            | `error` | 생성 실패 |

            ### 이벤트 흐름 예시
            ```
            event: connected
            event: progress     (analyzing)
            event: progress     (generating)
            event: file_created (directory: src/)
            event: file_created (file: Application.java)
            event: heartbeat
            ...
            event: complete
            ```
            """,
        operationId = "streamGenerateStatus",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "SSE 스트림",
                content = [
                    Content(
                        mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                        schema = Schema(implementation = String::class),
                        examples = [
                            ExampleObject(
                                name = "connected",
                                summary = "연결 수립",
                                value =
                                    """
                                    event: connected
                                    data: {"projectId": "550e8400-e29b-41d4-a716-446655440000", "message": "연결되었습니다. 생성을 시작합니다."}
                                    """,
                            ),
                            ExampleObject(
                                name = "heartbeat",
                                summary = "연결 유지",
                                value =
                                    """
                                    event: heartbeat
                                    data: ping
                                    """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun stream(
        @Parameter(description = "프로젝트 생성 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ResponseEntity<SseEmitter>

    @Operation(
        summary = "생성된 특정 파일 내용 조회",
        description =
            """
            생성된 프로젝트의 특정 파일 내용을 반환한다.
            `path`는 프로젝트 루트 기준 상대 경로를 사용한다.
            파일 경로에는 `/`가 포함될 수 있으므로 path variable이 아니라 query string으로 전달한다.
            비로그인 사용자도 접근 가능하다.
            """,
        operationId = "getGeneratedFileContent",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "파일 내용 조회 성공",
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
                                        "filePath": "backend/build.gradle",
                                        "content": "plugins {\n    id 'java'\n}",
                                        "language": "groovy"
                                      },
                                      "error": null
                                    }
                                    """,
                            ),
                        ],
                    ),
                ],
            ),
            SwaggerApiResponse(responseCode = "404", description = "프로젝트 ID 또는 파일 경로 없음"),
        ],
    )
    fun getFile(
        @Parameter(description = "프로젝트 생성 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
        @Parameter(description = "파일 경로 (프로젝트 루트 기준 상대 경로)", example = "backend/build.gradle")
        @RequestParam(name = "filePath") path: String,
    ): ResponseEntity<ApiResponse>

    @Operation(
        summary = "생성된 프로젝트 zip 다운로드 URL 발급",
        description =
            """
            생성된 프로젝트 전체를 zip으로 압축한 후 다운로드 URL을 반환한다.
            URL은 presigned 방식으로 만료 시간이 있으며, 직접 URL 노출을 방지한다. (@NFR-SEC-03)
            비로그인 사용자도 접근 가능하다.

            ### 파일명 규칙
            `{projectName}_{YYYYMMDD}.zip`
            프로젝트명에 특수문자가 포함된 경우 파일 시스템 안전 문자로 치환한다.
            """,
        operationId = "downloadGeneratedProject",
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
            SwaggerApiResponse(responseCode = "404", description = "프로젝트 ID 없음 또는 만료"),
            SwaggerApiResponse(responseCode = "500", description = "서버 내부 오류"),
        ],
    )
    fun issueDownloadUrl(
        @Parameter(description = "프로젝트 생성 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable projectId: UUID,
        @Parameter(hidden = true)
        request: HttpServletRequest,
    ): ResponseEntity<ApiResponse>
}
