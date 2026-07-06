package modeep.modev.global.ratelimit.util

object IgnoreRateLimit {
    fun shouldIgnore(
        method: String,
        path: String,
    ): Boolean {
        return isLogout(method, path) || isStructureStream(method, path)
    }

    private fun isLogout(
        method: String,
        path: String,
    ): Boolean {
        return method == "POST" && path == LOGOUT_PATH
    }

    private fun isStructureStream(
        method: String,
        path: String,
    ): Boolean {
        return method == "GET" && STRUCTURE_STREAM_PATH.matches(path)
    }

    private val STRUCTURE_STREAM_PATH = Regex("^/projects/structures/[^/]+/stream$")
    private const val LOGOUT_PATH = "/auth/logout"
}
