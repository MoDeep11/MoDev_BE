package modeep.modev.domain.auth.controller.dto.response

import modeep.modev.domain.auth.entity.User

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: LoginUserResponse,
)

data class LoginUserResponse(
    val userId: String,
    val email: String,
    val status: String,
) {
    companion object {
        fun from(user: User) =
            LoginUserResponse(
                userId = user.publicId,
                email = user.email,
                status = user.status.name,
            )
    }
}
