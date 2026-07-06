package modeep.modev.global.ratelimit

import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Duration

@Service
class RateLimitService(
    private val proxyManager: ProxyManager<ByteArray>,
) {
    fun tryConsume(
        key: String,
        policy: RateLimitPolicy,
    ): Boolean {
        val bucket = proxyManager.builder().build(redisKey(key, policy)) { bucketConfiguration(policy) }
        return bucket.tryConsume(1)
    }

    private fun bucketConfiguration(policy: RateLimitPolicy): BucketConfiguration {
        return BucketConfiguration.builder()
            .addLimit { limit ->
                limit
                    .capacity(policy.capacity)
                    .refillIntervally(
                        policy.refillTokens,
                        Duration.ofSeconds(policy.refillSeconds),
                    )
                    .id(policy.name)
            }
            .build()
    }

    private fun redisKey(
        key: String,
        policy: RateLimitPolicy,
    ): ByteArray = "$KEY_PREFIX$key:${policy.name}".toByteArray(StandardCharsets.UTF_8)

    private companion object {
        const val KEY_PREFIX = "rate-limit:"
    }
}
