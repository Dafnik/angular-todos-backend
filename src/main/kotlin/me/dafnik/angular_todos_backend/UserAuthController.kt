package me.dafnik.angular_todos_backend

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.web.bind.annotation.*

val refreshTokens = mutableMapOf<String, Long>()

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "User Auth API")
class UserAuthController(
    @Qualifier("userAuthenticationProvider") val daoAuthenticationProvider: DaoAuthenticationProvider,
    val userAccessTokenService: UserAccessTokenGenerationService,
    val userService: UserService,
) {
    @Suppress("DuplicatedCode")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: UserLoginDto): JwtResponse {
        val authentication = daoAuthenticationProvider.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )

        val user = userService.getByEmail(authentication.name) ?: throw UnauthorizedException()

        val refreshToken = RandomGenerator.string(40)
        refreshTokens[refreshToken] = user.id

        return JwtResponse(
            accessToken = userAccessTokenService.createToken(user.id.toString()),
            refreshToken = refreshToken
        )
    }

    @Suppress("DuplicatedCode")
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refresh(@Valid @RequestBody request: RefreshJwtWithSessionTokenDto): JwtResponse {
        val userId = refreshTokens[request.refreshToken] ?: throw UnauthorizedException()

        val user = userService.getById(userId) ?: throw UnauthorizedException()

        val refreshToken = RandomGenerator.string(40)
        refreshTokens[refreshToken] = user.id

        refreshTokens.remove(request.refreshToken)

        return JwtResponse(
            accessToken = userAccessTokenService.createToken(userId.toString()),
            refreshToken = refreshToken
        )
    }
}
