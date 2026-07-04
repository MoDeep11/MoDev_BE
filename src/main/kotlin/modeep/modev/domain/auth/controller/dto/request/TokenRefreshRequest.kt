package modeep.modev.domain.auth.controller.dto.request

import jakarta.validation.constraints.NotBlank

data class TokenRefreshRequest(
    @field:NotBlank
    val refreshToken: String,
)
