package me.dafnik.angular_todos_backend

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

/**
 * TokenService is responsible for creating JWT tokens based on the authentication
 * This class must be subclassed to be used
 *
 * @author Alexander Kauer
 * @since 2.0.0
 */
abstract class TokenGenerationService(
    private val jwtEncoder: JwtEncoder,
    private val validDuration: Duration
) {
    fun createToken(subject: String): String {
        val now = Instant.now()
        val expirationTime = now.plusSeconds(validDuration.inWholeSeconds)

        val claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(expirationTime)
            .subject(subject)
            .build()

        return jwtEncoder.encode(
            JwtEncoderParameters.from(claims)
        ).tokenValue
    }
}

/**
 * UserAccessTokenService creates a TokenService for creating JWT access tokens
 * @see TokenGenerationService
 * @since 2.0.0
 */
@Service
class UserAccessTokenGenerationService(
    @Qualifier("userJwtAccessTokenEncoder") jwtEncoder: JwtEncoder
) : TokenGenerationService(jwtEncoder, 60.minutes)

/**
 * UserRefreshTokenService creates a TokenService for creating JWT refresh tokens
 * @see TokenGenerationService
 * @since 2.0.0
 */
@Service
class UserRefreshTokenGenerationService(
    @Qualifier("userJwtRefreshTokenEncoder") jwtEncoder: JwtEncoder
) : TokenGenerationService(jwtEncoder, 30.days)
