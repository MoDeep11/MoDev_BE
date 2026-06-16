package modeep.modev.domain.auth.controller.dto.request

data class SignupRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
)
