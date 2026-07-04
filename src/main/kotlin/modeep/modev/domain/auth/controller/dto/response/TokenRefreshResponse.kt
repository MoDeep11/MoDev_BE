package modeep.modev.domain.auth.controller.dto.response

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
) {
    companion object {
        fun from(
            accessToken: String,
            refreshToken: String,
            expiresIn: Long,
        ) = TokenRefreshResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = expiresIn,
        )
    }
}
