package modeep.modev.domain.auth.controller.dto.response

import modeep.modev.domain.user.entity.User

data class SignupResponse(
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: String,
    val user: SignupUserResponse,
) {
    companion object {
        fun from(
            accessToken: String,
            refreshToken: String,
            expiresIn: Long,
            user: User,
        ) = SignupResponse(
            accessToken = accessToken,
            expiresIn = expiresIn,
            refreshToken = refreshToken,
            user = SignupUserResponse.from(user),
        )
    }
}

data class SignupUserResponse(
    val userId: Long,
    val email: String,
    val status: String,
) {
    companion object {
        fun from(user: User) =
            SignupUserResponse(
                userId = user.id,
                email = user.email,
                status = user.status.name,
            )
    }
}
