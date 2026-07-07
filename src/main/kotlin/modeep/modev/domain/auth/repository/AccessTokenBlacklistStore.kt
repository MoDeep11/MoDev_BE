package modeep.modev.domain.auth.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration

@Component
class AccessTokenBlacklistStore(
    private val redisTemplate: StringRedisTemplate,
) {
    fun save(
        accessToken: String,
        ttl: Duration,
    ) {
        if (ttl.isNegative || ttl.isZero) {
            return
        }

        redisTemplate.opsForValue().set(key(accessToken), BLACKLIST_VALUE, ttl)
    }

    fun exists(accessToken: String): Boolean = redisTemplate.hasKey(key(accessToken)) == true

    private fun key(accessToken: String): String = "$KEY_PREFIX${sha256(accessToken)}"

    private fun sha256(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val KEY_PREFIX = "auth:blacklist:"
        const val BLACKLIST_VALUE = "true"
    }
}
