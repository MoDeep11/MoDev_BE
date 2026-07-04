package modeep.modev.domain.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import modeep.modev.domain.auth.controller.dto.request.EmailVerificationSendRequest
import modeep.modev.domain.auth.controller.dto.request.LoginRequest
import modeep.modev.domain.auth.controller.dto.request.SignupRequest
import modeep.modev.domain.auth.controller.dto.request.TokenRefreshRequest
import modeep.modev.domain.auth.controller.dto.request.VerifyCode
import modeep.modev.global.response.ApiResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@Tag(name = "Auth", description = "인증 관련 API")
interface AuthControllerDocs {
    @Operation(
        summary = "회원가입",
        description = "이메일과 비밀번호로 신규 계정을 생성한다. 생성 직후 이메일 인증이 필요하다.",
        operationId = "signup",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "회원가입 성공",
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
                                        "accessToken": "eyJhbGci...",
                                        "expiresIn": 3600,
                                        "refreshToken": "eyJhbGci...",
                                        "user": {
                                          "userId": 1,
                                          "email": "user@example.com",
                                          "status": "UNVERIFIED"
                                        }
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
            SwaggerApiResponse(responseCode = "409", description = "이메일 중복"),
        ],
    )
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ApiResponse

    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인하고 액세스 토큰을 반환한다. 리프레시 토큰은 HttpOnly 쿠키로 설정한다.",
        operationId = "login",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "로그인 성공",
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
                                        "accessToken": "jwt.access.token",
                                        "expiresIn": 3600,
                                        "refreshToken": "jwt.refresh.token",
                                        "user": {
                                          "userId": 1,
                                          "email": "user@example.com",
                                          "status": "ACTIVE"
                                        }
                                      },
                                      "error": null
                                    }
                                    """,
                            ),
                        ],
                    ),
                ],
                headers = [
                    Header(
                        name = "Set-Cookie",
                        description = "refreshToken HttpOnly 쿠키를 설정한다. Path=/auth; Secure; SameSite=Strict",
                        schema = Schema(type = "string"),
                    ),
                ],
            ),
            SwaggerApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치"),
            SwaggerApiResponse(responseCode = "403", description = "이메일 미인증 계정 또는 계정 잠금"),
        ],
    )
    fun login(
        @Valid @RequestBody request: LoginRequest,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse

    @Operation(
        summary = "액세스 토큰 및 리프레시 토큰 재발급",
        description = "HttpOnly 쿠키의 리프레시 토큰을 검증하고 새 토큰을 발급한다.",
        operationId = "refreshToken",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "토큰 재발급 성공",
                headers = [
                    Header(
                        name = "Set-Cookie",
                        description = "새 refreshToken HttpOnly 쿠키를 설정한다. Path=/auth; Secure; SameSite=Strict",
                        schema = Schema(type = "string"),
                    ),
                ],
            ),
            SwaggerApiResponse(responseCode = "401", description = "리프레시 토큰 오류"),
        ],
    )
    fun refreshToken(
        @Parameter(description = "Expired access token. Format: Bearer {accessToken}")
        @RequestHeader(name = "Authorization", defaultValue = "") authorization: String,
        @Valid @RequestBody request: TokenRefreshRequest,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse

    @Operation(
        summary = "이메일 인증 코드 발송",
        description = "회원가입 이메일로 6자리 인증 코드를 발송한다.",
        operationId = "sendVerificationCode",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "발송 성공"),
            SwaggerApiResponse(responseCode = "409", description = "이미 인증된 계정"),
            SwaggerApiResponse(responseCode = "429", description = "재발송 Rate Limit 초과"),
        ],
    )
    fun sendVerificationCode(
        @Valid @RequestBody request: EmailVerificationSendRequest,
    ): ApiResponse

    @Operation(
        summary = "이메일 인증 확인",
        description = "사용자가 입력한 이메일 인증 코드를 검증한다.",
        operationId = "verifyEmail",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            SwaggerApiResponse(responseCode = "400", description = "유효하지 않은 인증 코드"),
            SwaggerApiResponse(responseCode = "409", description = "이미 인증된 계정"),
            SwaggerApiResponse(responseCode = "410", description = "인증 코드 만료"),
        ],
    )
    fun verifyAuthCode(
        @Valid @RequestBody request: VerifyCode,
    ): ApiResponse

    @Operation(
        summary = "로그아웃",
        description = "리프레시 토큰을 무효화하고 쿠키를 삭제한다.",
        operationId = "logout",
    )
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "로그아웃 성공",
                headers = [
                    Header(
                        name = "Set-Cookie",
                        description = "refreshToken 쿠키를 만료한다. Path=/auth 및 Path=/auth/token/refresh; Max-Age=0",
                        schema = Schema(type = "string"),
                    ),
                ],
            ),
        ],
    )
    fun logout(
        @Parameter(hidden = true)
        @CookieValue(name = "refreshToken", defaultValue = "") refreshToken: String,
        @Parameter(hidden = true) response: HttpServletResponse,
    ): ApiResponse
}
