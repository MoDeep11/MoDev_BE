package modeep.modev.domain.auth.controller.dto.response

import modeep.modev.domain.user.entity.User

@Deprecated("Use LoginResponse instead")
data class SignupResponse(
    val userId: Long,
    val email: String,
    val status: String,
    val role: String,
) {
    companion object {
        fun from(user: User) =
            SignupResponse(
                userId = user.id,
                email = user.email,
                status = user.status.name,
                role = user.role.name,
            )
    }
}
