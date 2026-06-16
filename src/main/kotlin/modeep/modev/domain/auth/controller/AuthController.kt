package modeep.modev.domain.auth.controller

import modeep.modev.domain.auth.controller.dto.request.LoginRequest
import modeep.modev.domain.auth.controller.dto.request.SignupRequest
import modeep.modev.domain.auth.service.LoginService
import modeep.modev.domain.auth.service.SignupService
import modeep.modev.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val signupService: SignupService,
    private val loginService: LoginService,
) {
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(
        @RequestBody request: SignupRequest,
    ): ApiResponse =
        ApiResponse(
            success = true,
            data = signupService.execute(request),
        )

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
    ): ApiResponse =
        ApiResponse(
            success = true,
            data = loginService.execute(request),
        )
}
