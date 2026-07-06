package modeep.modev.domain.auth.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration

@Component
class RefreshTokenStore(
    private val redisTemplate: StringRedisTemplate,
) {
    fun save(
        refreshToken: String,
        userId: String,
        ttl: Duration,
    ) {
        redisTemplate
            .opsForValue()
            .set(key(refreshToken), userId, ttl)
    }

    fun rotate(
        currentRefreshToken: String,
        newRefreshToken: String,
        userId: String,
        ttl: Duration,
    ): Boolean {
        val result =
            redisTemplate.execute(
                ROTATE_SCRIPT,
                listOf(key(currentRefreshToken), key(newRefreshToken)),
                userId,
                ttl.toMillis().toString(),
            )

        return result == 1L
    }

    fun delete(refreshToken: String) {
        redisTemplate.delete(key(refreshToken))
    }

    private fun key(refreshToken: String): String = "$KEY_PREFIX${sha256(refreshToken)}"

    private fun sha256(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val KEY_PREFIX = "auth:refresh:"

        val ROTATE_SCRIPT =
            DefaultRedisScript(
                """
                if redis.call('GET', KEYS[1]) ~= ARGV[1] then
                    return 0
                end
                redis.call('DEL', KEYS[1])
                redis.call('PSETEX', KEYS[2], ARGV[2], ARGV[1])
                return 1
                """.trimIndent(),
                Long::class.java,
            )
    }
}
