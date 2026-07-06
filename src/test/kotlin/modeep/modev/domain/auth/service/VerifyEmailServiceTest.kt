package modeep.modev.domain.auth.service

import modeep.modev.domain.auth.controller.dto.request.VerifyEmailResponse
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VerifyEmailServiceTest {
    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var valueOperations: ValueOperations<String, String>
    private lateinit var service: VerifyEmailService

    @BeforeEach
    fun setUp() {
        @Suppress("UNCHECKED_CAST")
        redisTemplate = mock(RedisTemplate::class.java) as RedisTemplate<String, String>
        @Suppress("UNCHECKED_CAST")
        valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
        service = VerifyEmailService(redisTemplate)
    }

    @Test
    fun `verifies code, stores verified marker, and deletes saved code`() {
        `when`(valueOperations.get("auth:email-verification:code:user@example.com")).thenReturn("012345")

        service.execute(
            VerifyEmailResponse(
                email = " User@Example.com ",
                code = "012345",
            ),
        )

        verify(valueOperations)
            .set(
                "auth:email-verification:verified:user@example.com",
                "true",
                Duration.ofMinutes(30),
            )
        verify(redisTemplate).delete("auth:email-verification:code:user@example.com")
    }

    @Test
    fun `rejects invalid verification code without deleting saved code`() {
        `when`(valueOperations.get("auth:email-verification:code:user@example.com")).thenReturn("012345")

        val exception =
            assertFailsWith<BusinessException> {
                service.execute(
                    VerifyEmailResponse(
                        email = "user@example.com",
                        code = "999999",
                    ),
                )
            }

        assertEquals(AuthErrorCode.VERIFY_CODE_INVALID, exception.errorCode)
        verify(redisTemplate, never()).delete("auth:email-verification:code:user@example.com")
    }

    @Test
    fun `rejects expired verification code`() {
        `when`(valueOperations.get("auth:email-verification:code:user@example.com")).thenReturn(null)

        val exception =
            assertFailsWith<BusinessException> {
                service.execute(
                    VerifyEmailResponse(
                        email = "user@example.com",
                        code = "012345",
                    ),
                )
            }

        assertEquals(AuthErrorCode.VERIFY_CODE_EXPIRED, exception.errorCode)
    }
}
