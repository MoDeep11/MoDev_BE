package modeep.modev.domain.auth.controller.dto.response

import modeep.modev.domain.auth.entity.User

data class SignupResponse(
    val userId: String,
    val email: String,
    val status: String,
) {
    companion object {
        fun from(user: User) =
            SignupResponse(
                userId = user.publicId,
                email = user.email,
                status = user.status.name,
            )
    }
}
