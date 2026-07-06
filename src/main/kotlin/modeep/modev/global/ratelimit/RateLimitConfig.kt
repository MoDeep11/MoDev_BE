package modeep.modev.global.ratelimit

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce
import io.lettuce.core.RedisClient
import io.lettuce.core.cluster.RedisClusterClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration

@Configuration
class RateLimitConfig {
    private companion object {
        val RATE_LIMIT_EXPIRATION_BUFFER: Duration = Duration.ofMinutes(1)
    }

    @Bean
    fun rateLimitProxyManager(redisConnectionFactory: LettuceConnectionFactory): ProxyManager<ByteArray> {
        val builder =
            when (val nativeClient = redisConnectionFactory.requiredNativeClient) {
                is RedisClient -> Bucket4jLettuce.casBasedBuilder(nativeClient)
                is RedisClusterClient -> Bucket4jLettuce.casBasedBuilder(nativeClient)
                else -> error("Unsupported Lettuce native client: ${nativeClient::class.qualifiedName}")
            }

        return builder
            .expirationAfterWrite(
                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(RATE_LIMIT_EXPIRATION_BUFFER),
            )
            .build()
    }
}
