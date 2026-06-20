package modeep.modev.domain.auth.controller.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class VerifyCode(
    @NotBlank
    @Email
    val email: String,
    @NotBlank
    @Pattern(regexp = "^\\d{6}$")
    val code: String,
)
