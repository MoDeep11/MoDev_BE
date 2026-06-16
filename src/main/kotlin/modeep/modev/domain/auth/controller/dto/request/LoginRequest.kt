package modeep.modev.domain.auth.controller.dto.request

data class LoginRequest(
    val email: String,
    val password: String,
)
