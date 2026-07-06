package modeep.modev.domain.auth.controller.dto.response

data class TokenRefreshResponse(
    val accessToken: String,
    val expiresIn: Long,
)
