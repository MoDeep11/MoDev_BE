package modeep.modev.global.ratelimit

import jakarta.servlet.http.HttpServletRequest
import modeep.modev.global.ratelimit.util.IgnoreRateLimit.shouldIgnore
import modeep.modev.global.util.clientIp
import modeep.modev.global.util.isAuthenticatedUser
import org.springframework.security.core.context.SecurityContextHolder

data class RateLimitTarget(
    val policies: List<RateLimitPolicy>,
    val key: String,
)

fun HttpServletRequest.resolveRateLimitTarget(): RateLimitTarget? {
    val method = this.method
    val path = this.requestURI
    val clientIp = this.clientIp()

    return when {
        method == "POST" && path == "/auth/login" ->
            RateLimitTarget(
                policies = listOf(RateLimitPolicy.AUTH),
                key = "auth:$clientIp",
            )

        method == "POST" && path == "/auth/signup" ->
            RateLimitTarget(
                policies = listOf(RateLimitPolicy.AUTH),
                key = "signup:$clientIp",
            )

        method == "POST" && path == "/auth/code/verify" ->
            RateLimitTarget(
                policies = listOf(RateLimitPolicy.AUTH),
                key = "verify-code:$clientIp",
            )

        method == "POST" && path == "/auth/reissue" ->
            RateLimitTarget(
                policies = listOf(RateLimitPolicy.TOKEN_REFRESH),
                key = "token-refresh:$clientIp",
            )

        method == "POST" && path == "/auth/email" ->
            RateLimitTarget(
                policies =
                    listOf(
                        RateLimitPolicy.EMAIL_SEND_PER_MINUTE,
                        RateLimitPolicy.EMAIL_SEND_PER_DAY,
                    ),
                key = "email-send:$clientIp",
            )

        method == "POST" && path == "/projects/structures" ->
            RateLimitTarget(
                policies = listOf(RateLimitPolicy.AI_GENERATION),
                key = "ai-generation:${principalKey(this)}",
            )

        method == "GET" && path.endsWith(".zip") ->
            RateLimitTarget(
                policies = listOf(RateLimitPolicy.ZIP_DOWNLOAD),
                key = "zip-download:${principalKey(this)}",
            )

        shouldIgnore(path) ->
            null

        else ->
            RateLimitTarget(
                policies = listOf(RateLimitPolicy.GENERAL_API),
                key = "general:${principalKey(this)}",
            )
    }
}

private fun principalKey(request: HttpServletRequest): String {
    val authentication = SecurityContextHolder.getContext().authentication

    return if (authentication.isAuthenticatedUser()) {
        "user:${authentication.name}"
    } else {
        "ip:${request.clientIp()}"
    }
}
