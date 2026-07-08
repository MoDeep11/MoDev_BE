package modeep.modev.global.response

data class InfoLog(
    val traceId: String?,
    val userId: String?,
    val clientIp: String?,
    val method: String,
    val path: String,
    val query: String?,
    val status: String,
    val durationMs: String,
)
