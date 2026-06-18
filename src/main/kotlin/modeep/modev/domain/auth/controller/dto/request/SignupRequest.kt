package modeep.modev.domain.auth.controller.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SignupRequest(
    @Email
    val email: String,
    @NotBlank
    val password: String,
    val passwordConfirm: String,
)
