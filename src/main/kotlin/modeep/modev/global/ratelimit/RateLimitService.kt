package modeep.modev.global.ratelimit

import io.github.bucket4j.Bucket
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Service
class RateLimitService {
    private val buckets = ConcurrentHashMap<String, Bucket>()

    fun tryConsume(
        key: String,
        policy: RateLimitPolicy,
    ): Boolean {
        val bucket =
            buckets.computeIfAbsent("$key:${policy.name}") {
                newBucket(policy)
            }
        return bucket.tryConsume(1)
    }

    private fun newBucket(policy: RateLimitPolicy): Bucket {
        return Bucket.builder()
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
}
