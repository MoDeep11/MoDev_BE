package modeep.modev.global.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import modeep.modev.domain.user.entity.User
import modeep.modev.global.exception.BusinessException
import modeep.modev.global.exception.error.AuthErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import java.util.UUID

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") secret: String,
    @param:Value("\${jwt.access-token-expiration:3600000}") private val accessTokenExpiration: Long,
    @param:Value("\${jwt.refresh-token-expiration:1209600000}") private val refreshTokenExpiration: Long,
) {
    private val signingKey =
        secret
            .toByteArray(StandardCharsets.UTF_8)
            .also {
                require(it.size >= MINIMUM_HMAC_KEY_SIZE_BYTES) {
                    "jwt.secret must be at least $MINIMUM_HMAC_KEY_SIZE_BYTES bytes"
                }
            }.let(Keys::hmacShaKeyFor)

    val accessTokenExpiresInSeconds: Long
        get() = accessTokenExpiration / 1000

    val refreshTokenExpiresInSeconds: Long
        get() = refreshTokenExpiration / 1000

    val refreshTokenExpirationMillis: Long
        get() = refreshTokenExpiration

    fun generateAccessToken(user: User): String {
        return generateToken(user, TokenType.ACCESS, accessTokenExpiration)
    }

    fun generateRefreshToken(user: User): String {
        return generateToken(user, TokenType.REFRESH, refreshTokenExpiration)
    }

    fun parseAccessToken(token: String): JwtPrincipal {
        val claims = parseClaims(token)
        if (claims.tokenType() != TokenType.ACCESS.value) {
            throw JwtException("invalid token type")
        }

        return JwtPrincipal(
            userId = claims.subject,
            status =
                (claims["status"] as? String)
                    ?: throw JwtException("missing or invalid status claim"),
            role =
                (claims["role"] as? String)
                    ?: throw JwtException("missing or invalid role claim"),
        )
    }

    fun parseRefreshToken(token: String): String {
        val claims =
            try {
                parseClaims(token)
            } catch (exception: ExpiredJwtException) {
                throw BusinessException(AuthErrorCode.REFRESH_TOKEN_EXPIRED, cause = exception)
            } catch (exception: JwtException) {
                throw BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID, cause = exception)
            } catch (exception: IllegalArgumentException) {
                throw BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID, cause = exception)
            }

        if (claims.tokenType() != TokenType.REFRESH.value) {
            throw BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID)
        }

        return claims.subject
    }

    private fun generateToken(
        user: User,
        tokenType: TokenType,
        expirationMillis: Long,
    ): String {
        val issuedAt = Instant.now()

        return Jwts
            .builder()
            .subject(user.id.toString())
            .claim("type", tokenType.value)
            .claim("status", user.status.name)
            .claim("role", user.role.name)
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(issuedAt.plusMillis(expirationMillis)))
            .signWith(signingKey)
            .compact()
    }

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload

    private fun Claims.tokenType(): String? = this["type"] as? String

    private enum class TokenType(
        val value: String,
    ) {
        ACCESS("access"),
        REFRESH("refresh"),
    }

    private companion object {
        const val MINIMUM_HMAC_KEY_SIZE_BYTES = 32
    }
}
