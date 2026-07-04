package modeep.modev.global.security.jwt

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class JwtAuthenticationFilterTest {
    private val jwtTokenProvider = mock(JwtTokenProvider::class.java)
    private val jwtAuthenticationEntryPoint = mock(JwtAuthenticationEntryPoint::class.java)
    private val filter = JwtAuthenticationFilter(jwtTokenProvider, jwtAuthenticationEntryPoint)

    @Test
    fun `does not parse authorization header on token refresh endpoint`() {
        val request =
            MockHttpServletRequest("POST", "/auth/token/refresh").apply {
                addHeader("Authorization", "Bearer expired-access-token")
            }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        verifyNoInteractions(jwtTokenProvider, jwtAuthenticationEntryPoint)
    }

    @Test
    fun `does not parse authorization header on token refresh endpoint with context path`() {
        val request =
            MockHttpServletRequest("POST", "/api/auth/token/refresh").apply {
                contextPath = "/api"
                addHeader("Authorization", "Bearer expired-access-token")
            }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        verifyNoInteractions(jwtTokenProvider, jwtAuthenticationEntryPoint)
    }

    @Test
    fun `does not parse authorization header on token refresh endpoint with proxy prefix`() {
        val request =
            MockHttpServletRequest("POST", "/api/auth/token/refresh").apply {
                addHeader("Authorization", "Bearer expired-access-token")
            }
        val response = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(request, response, chain)

        verifyNoInteractions(jwtTokenProvider, jwtAuthenticationEntryPoint)
    }
}
