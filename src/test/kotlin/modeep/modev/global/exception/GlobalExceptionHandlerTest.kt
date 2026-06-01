package modeep.modev.global.exception

import com.fasterxml.jackson.databind.ObjectMapper
import modeep.modev.global.exception.error.AuthErrorCode
import modeep.modev.global.exception.error.GlobalErrorCode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler(ObjectMapper())

    @BeforeEach
    fun setUp() {
        MDC.put("traceId", "trace-1")
        MDC.put("userId", "user-1")
        MDC.put("clientIp", "127.0.0.1")
    }

    @AfterEach
    fun tearDown() {
        MDC.clear()
    }

    @Test
    fun `handles business exception with its error code`() {
        val request =
            MockHttpServletRequest("POST", "/auth/login").apply {
                queryString = "redirect=/projects"
            }
        val exception = BusinessException(AuthErrorCode.INVALID_CREDENTIALS)

        val response = handler.handlerBusinessException(exception, request)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)

        val body = assertNotNull(response.body)
        assertFalse(body.success)
        assertNull(body.data)

        val error = assertNotNull(body.error)
        assertSame(AuthErrorCode.INVALID_CREDENTIALS, error.code)
        assertEquals(AuthErrorCode.INVALID_CREDENTIALS.message, error.message)
    }

    @Test
    fun `handles unexpected exception as internal error`() {
        val request = MockHttpServletRequest("GET", "/unknown")
        val exception = RuntimeException("boom")

        val response = handler.handleException(exception, request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)

        val body = assertNotNull(response.body)
        assertFalse(body.success)
        assertNull(body.data)

        val error = assertNotNull(body.error)
        assertSame(GlobalErrorCode.INTERNAL_ERROR, error.code)
        assertEquals(GlobalErrorCode.INTERNAL_ERROR.message, error.message)
    }
}
