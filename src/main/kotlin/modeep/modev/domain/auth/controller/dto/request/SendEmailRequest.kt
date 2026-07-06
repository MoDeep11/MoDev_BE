package modeep.modev.domain.auth.controller.dto.request
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SendEmailRequest(
    @field:NotBlank
    @field:Email
    val email: String,
)
