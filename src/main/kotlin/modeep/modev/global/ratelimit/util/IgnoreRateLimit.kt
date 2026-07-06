package modeep.modev.global.ratelimit.util

object IgnoreRateLimit {
    fun shouldIgnore(path: String): Boolean {
        return isLogout(path) || isSse(path)
    }

    private fun isLogout(path: String): Boolean {
        return path == "/auth/logout"
    }

    private fun isSse(path: String): Boolean {
        return path.endsWith("/stream")
    }
}
