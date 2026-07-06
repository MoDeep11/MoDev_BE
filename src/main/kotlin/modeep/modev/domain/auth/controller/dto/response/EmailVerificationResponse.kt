package modeep.modev.domain.auth.controller.dto.response

import modeep.modev.domain.user.entity.User

@Deprecated("Not Used Anywhere")
data class EmailVerificationResponse(
    val userId: String,
    val status: String,
) {
    companion object {
        fun from(user: User): EmailVerificationResponse =
            EmailVerificationResponse(
                userId = "user_${user.id}",
                status = user.status.name,
            )
    }
}
