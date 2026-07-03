package modeep.modev.domain.auth.controller.dto.response

import com.fasterxml.jackson.annotation.JsonIgnore
import modeep.modev.domain.user.entity.User

data class LoginResponse(
    val accessToken: String,
    @get:JsonIgnore
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val refreshExpiresIn: Long,
    val user: LoginUserResponse,
)

data class LoginUserResponse(
    val userId: Long,
    val email: String,
    val status: String,
) {
    companion object {
        fun from(user: User) =
            LoginUserResponse(
                userId = user.id,
                email = user.email,
                status = user.status.name,
            )
    }
}
