package modeep.modev.domain.auth.controller.dto.response

import modeep.modev.domain.user.entity.User

data class LoginResponse(
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: String,
    val user: LoginUserResponse,
) {
    companion object {
        fun from(
            accessToken: String,
            refreshToken: String,
            expiresIn: Long,
            user: User,
        ) = LoginResponse(
            accessToken = accessToken,
            expiresIn = expiresIn,
            refreshToken = refreshToken,
            user = LoginUserResponse.from(user),
        )
    }
}

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
