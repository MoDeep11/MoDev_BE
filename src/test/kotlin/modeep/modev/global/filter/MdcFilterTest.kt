package modeep.modev.global.filter

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MdcFilterTest {
    private val filter = MdcFilter(ObjectMapper())

    @AfterTest
    fun tearDown() {
        MDC.clear()
    }

    @Test
    fun `puts trace id and client ip while filter chain runs`() {
        val request =
            MockHttpServletRequest("GET", "/health").apply {
                remoteAddr = "192.168.0.10"
            }
        val response = MockHttpServletResponse()
        val filterChain = CapturingFilterChain()

        filter.doFilter(request, response, filterChain)

        assertTrue(filterChain.called)
        assertNotNull(filterChain.traceId)
        assertEquals("192.168.0.10", filterChain.clientIp)
        assertNull(filterChain.userId)
    }

    @Test
    fun `clears mdc after filter chain completes`() {
        val request =
            MockHttpServletRequest("GET", "/health").apply {
                remoteAddr = "192.168.0.10"
            }
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, CapturingFilterChain())

        assertNull(MDC.get("traceId"))
        assertNull(MDC.get("clientIp"))
        assertNull(MDC.get("userId"))
    }

    private class CapturingFilterChain : FilterChain {
        var called = false
            private set
        var traceId: String? = null
            private set
        var clientIp: String? = null
            private set
        var userId: String? = null
            private set

        override fun doFilter(
            request: ServletRequest,
            response: ServletResponse,
        ) {
            called = true
            traceId = MDC.get("traceId")
            clientIp = MDC.get("clientIp")
            userId = MDC.get("userId")
        }
    }
}
