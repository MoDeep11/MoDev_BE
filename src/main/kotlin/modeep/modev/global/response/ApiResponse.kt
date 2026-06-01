package modeep.modev.global.response

data class ApiResponse(
    val success: Boolean,
    val data: Any?,
    val error: ErrorResponse? = null,
)
