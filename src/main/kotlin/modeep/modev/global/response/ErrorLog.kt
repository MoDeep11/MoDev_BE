package modeep.modev.global.response

data class ErrorLog(
    val event: String,
    val errorCode: String,
    val traceId: String?,
    val userId: String?,
    val clientIp: String?,
    val method: String,
    val path: String,
    val query: String?,
    val exception: String,
    val message: String?,
)
