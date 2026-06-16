package modeep.modev.global.security.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import modeep.modev.domain.auth.entity.User
import modeep.modev.domain.auth.entity.UserStatus
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JwtTokenProviderTest {
    @Test
    fun `generates an access token with user claims`() {
        val secret = "test-secret-key-that-is-at-least-32-bytes-long"
        val provider = JwtTokenProvider(secret, 3_600_000)
        val user =
            User(
                publicId = "user_123",
                email = "user@example.com",
                passwordHash = "encoded-password",
                status = UserStatus.ACTIVE,
            )

        val token = provider.generateAccessToken(user)
        val claims =
            Jwts
                .parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .payload

        assertEquals("user_123", claims.subject)
        assertEquals("user@example.com", claims["email"])
        assertEquals("ACTIVE", claims["status"])
        assertTrue(claims.expiration.time - claims.issuedAt.time in 3_599_000..3_600_000)
        assertEquals(3600, provider.accessTokenExpiresInSeconds)
    }

    @Test
    fun `rejects a secret shorter than 32 bytes`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                JwtTokenProvider("too-short", 3_600_000)
            }

        assertEquals("jwt.secret must be at least 32 bytes", exception.message)
    }
}
