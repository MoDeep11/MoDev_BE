package modeep.modev.global.exception

import modeep.modev.global.exception.error.AuthErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class BusinessExceptionTest {
    @Test
    fun `stores error code message details and cause`() {
        val cause = IllegalArgumentException("invalid input")
        val details = mapOf("field" to "email")

        val exception =
            BusinessException(
                errorCode = AuthErrorCode.INVALID_CREDENTIALS,
                message = "login failed",
                details = details,
                cause = cause,
            )

        assertSame(AuthErrorCode.INVALID_CREDENTIALS, exception.errorCode)
        assertEquals("login failed", exception.message)
        assertEquals(details, exception.details)
        assertSame(cause, exception.cause)
    }
}
