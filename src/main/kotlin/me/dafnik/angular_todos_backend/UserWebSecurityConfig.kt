package me.dafnik.angular_todos_backend

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint

/**
 * UserWebSecurityConfig creates all necessary configurations for Spring Security
 * It creates a JWT based authentication for users
 * The authentication and authorization process ist based on the OAuth2.0 standard
 *
 * @author Alexander Kauer
 * @since 1.0.0
 */
@Configuration
@Order(1)
class UserWebSecurityConfig(
    val keyUtils: KeyUtils
) {
    /**
     * SecurityFilterChain for user authentication
     */
    @Suppress("SpreadOperator")
    @Bean
    fun filterChainUser(
        http: HttpSecurity,
        userAuthDetailsService: UserAuthDetailsService,
        @Qualifier("userJwtAccessTokenDecoder") userJwtDecoder: JwtDecoder,
    ): SecurityFilterChain {
        http {
            cors { }
            csrf { disable() }

            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            securityMatcher(*Routes.USER_MATCHER)

            authorizeRequests {
                Routes.USER_UNSECURED.forEach {
                    authorize(it, permitAll)
                }

                authorize(anyRequest, authenticated)
            }

            oauth2ResourceServer {
                jwt {
                    val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
                    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("scope")
                    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("")

                    val jwtAuthenticationConverter = JwtAuthenticationConverter()
                    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter)

                    this.jwtAuthenticationConverter = jwtAuthenticationConverter
                    this.jwtDecoder = userJwtDecoder
                }
            }

            exceptionHandling {
                authenticationEntryPoint = HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
            }
        }

        http.userDetailsService(userAuthDetailsService)

        return http.build()
    }

    /**
     * Creates an ProviderManager
     * This can be consumed by authentication controllers to authenticate user with their credentials
     */
    @Bean("userAuthenticationProvider")
    @Qualifier("userAuthenticationProvider")
    fun userAuthenticationProvider(
        userAuthDetailsService: UserAuthDetailsService,
        passwordEncoder: PasswordEncoder
    ): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userAuthDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder)
        return authProvider
    }

    /**
     * JwtAuthenticationProvider for user access tokens
     * This is needed for every request to convert a JWT to a user
     */
    @Bean("userAccessTokenAuthProvider")
    fun userAccessTokenAuthProvider(
        @Qualifier("userJwtAccessTokenDecoder") jwtDecoder: JwtDecoder,
        jwtToUserConverter: JwtToUserConverter
    ): JwtAuthenticationProvider {
        val provider = JwtAuthenticationProvider(jwtDecoder)
        provider.setJwtAuthenticationConverter(jwtToUserConverter)
        return provider
    }

    /**
     * JwtAuthenticationProvider for user access tokens
     * This is needed for refresh tokens to convert a JWT to a user while refreshing
     */
    @Bean("userRefreshTokenAuthProvider")
    fun userRefreshTokenAuthProvider(
        @Qualifier("userJwtRefreshTokenDecoder") jwtDecoder: JwtDecoder,
        jwtToUserConverter: JwtToUserConverter
    ): JwtAuthenticationProvider {
        val provider = JwtAuthenticationProvider(jwtDecoder)
        provider.setJwtAuthenticationConverter(jwtToUserConverter)
        return provider
    }

    /**
     * Service to get an authenticated user from the database
     */
    @Bean(AuthUtils.USER_AUTH_DETAILS_SERVICE)
    fun userAuthDetailsService(userService: UserService): UserAuthDetailsService {
        return UserAuthDetailsService(userService)
    }

    /**
     * JwtDecoder for user access tokens
     */
    @Bean("userJwtAccessTokenDecoder")
    @Qualifier("userJwtAccessTokenDecoder")
    @Primary
    fun userJwtAccessTokenDecoder(): JwtDecoder {
        return NimbusJwtDecoder
            .withPublicKey(keyUtils.userAccessTokenPublicKey)
            .build()
    }

    /**
     * JwtEncoder for user access tokens
     */
    @Bean("userJwtAccessTokenEncoder")
    @Qualifier("userJwtAccessTokenEncoder")
    @Primary
    fun userJwtAccessTokenEncoder(): JwtEncoder {
        val jwk = RSAKey
            .Builder(keyUtils.userAccessTokenPublicKey)
            .privateKey(keyUtils.userAccessTokenPrivateKey)
            .build()

        return NimbusJwtEncoder(ImmutableJWKSet(JWKSet(jwk)))
    }

    /**
     * JwtDecoder for user refresh tokens
     */
    @Bean("userJwtRefreshTokenDecoder")
    @Qualifier("userJwtRefreshTokenDecoder")
    fun userJwtRefreshTokenDecoder(): JwtDecoder {
        return NimbusJwtDecoder
            .withPublicKey(keyUtils.userRefreshTokenPublicKey)
            .build()
    }

    /**
     * JwtEncoder for user refresh tokens
     */
    @Bean("userJwtRefreshTokenEncoder")
    @Qualifier("userJwtRefreshTokenEncoder")
    fun userJwtRefreshTokenEncoder(): JwtEncoder {
        val jwk = RSAKey
            .Builder(keyUtils.userRefreshTokenPublicKey)
            .privateKey(keyUtils.userRefreshTokenPrivateKey)
            .build()

        return NimbusJwtEncoder(ImmutableJWKSet(JWKSet(jwk)))
    }
}
