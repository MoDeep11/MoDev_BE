package modeep.modev.domain.auth.repository

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccessTokenBlacklistStoreTest {
    private val redisTemplate = mock(StringRedisTemplate::class.java)
    private val valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
    private val store = AccessTokenBlacklistStore(redisTemplate)

    @Test
    fun `saves access token blacklist with ttl`() {
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)

        store.save("access-token", Duration.ofMinutes(10))

        verify(valueOperations).set(
            org.mockito.ArgumentMatchers.startsWith("auth:blacklist:"),
            org.mockito.ArgumentMatchers.eq("true"),
            org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(10)),
        )
    }

    @Test
    fun `does not save access token blacklist when ttl is not positive`() {
        store.save("access-token", Duration.ZERO)

        verify(redisTemplate, never()).opsForValue()
    }

    @Test
    fun `checks blacklisted access token existence`() {
        `when`(redisTemplate.hasKey(org.mockito.ArgumentMatchers.startsWith("auth:blacklist:"))).thenReturn(true)

        assertTrue(store.exists("access-token"))
    }

    @Test
    fun `returns false when blacklist key is absent`() {
        `when`(redisTemplate.hasKey(org.mockito.ArgumentMatchers.startsWith("auth:blacklist:"))).thenReturn(false)

        assertFalse(store.exists("access-token"))
    }
}
