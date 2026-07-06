package modeep.modev.global.util

import jakarta.servlet.http.HttpServletRequest

fun HttpServletRequest.clientIp(): String {
    return this.getHeader("X-Forwarded-For")
        ?.split(",")
        ?.firstOrNull()
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: this.remoteAddr
}
