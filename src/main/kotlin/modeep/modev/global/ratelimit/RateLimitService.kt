package modeep.modev.global.ratelimit

import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RateLimitService(
    private val proxyManager: ProxyManager<ByteArray>,
) {
    fun tryConsume(
        key: String,
        policies: List<RateLimitPolicy>,
    ): Boolean {
        val bucket =
            proxyManager.builder().build(redisKey(key)) {
                bucketConfiguration(policies)
            }
        return bucket.tryConsume(1)
    }

    private fun bucketConfiguration(policies: List<RateLimitPolicy>): BucketConfiguration {
        val builder = BucketConfiguration.builder()
        policies.forEach { policy ->
            builder.addLimit { limit ->
                limit
                    .capacity(policy.capacity)
                    .refillIntervally(
                        policy.refillTokens,
                        Duration.ofSeconds(policy.refillSeconds),
                    )
                    .id(policy.name)
            }
        }
        return builder.build()
    }

    private fun redisKey(key: String): ByteArray = "$KEY_PREFIX$key".toByteArray(Charsets.UTF_8)

    private companion object {
        const val KEY_PREFIX = "rate-limit:"
    }
}
