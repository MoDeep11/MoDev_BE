package modeep.modev.global.response

import modeep.modev.global.exception.ErrorCode

data class ErrorResponse(
    val code: ErrorCode,
    val message: String,
)
