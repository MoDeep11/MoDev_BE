package modeep.modev.domain.auth.repository

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RefreshTokenStoreTest {
    private val redisTemplate = mock(StringRedisTemplate::class.java)
    private val valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
    private val store = RefreshTokenStore(redisTemplate)

    @Test
    fun `saves refresh token with ttl`() {
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)

        store.save("refresh-token", "1", Duration.ofDays(14))

        verify(valueOperations).set(
            org.mockito.ArgumentMatchers.startsWith("auth:refresh:"),
            org.mockito.ArgumentMatchers.eq("1"),
            org.mockito.ArgumentMatchers.eq(Duration.ofDays(14)),
        )
    }

    @Test
    fun `checks refresh token existence`() {
        `when`(redisTemplate.hasKey(org.mockito.ArgumentMatchers.startsWith("auth:refresh:"))).thenReturn(true)

        assertTrue(store.exists("refresh-token"))
    }

    @Test
    fun `returns false when refresh token key is absent`() {
        `when`(redisTemplate.hasKey(org.mockito.ArgumentMatchers.startsWith("auth:refresh:"))).thenReturn(false)

        assertFalse(store.exists("refresh-token"))
    }
}
