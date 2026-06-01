package modeep.modev.global.exception

open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String? = null,
    val details: Any? = null,
    override val cause: Throwable? = null,
) : RuntimeException()
