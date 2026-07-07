package modeep.modev.global.security.jwt

data class JwtPrincipal(
    val userId: String,
    val status: String,
    val role: String,
)
