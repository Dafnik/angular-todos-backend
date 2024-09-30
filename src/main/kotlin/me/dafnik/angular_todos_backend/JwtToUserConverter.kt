package me.dafnik.angular_todos_backend

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class JwtToUserConverter(
    val userService: UserService
) : Converter<Jwt, UsernamePasswordAuthenticationToken?> {
    override fun convert(jwt: Jwt): UsernamePasswordAuthenticationToken {
        val user = userService.getByEmail(jwt.subject) ?: throw UnauthorizedException()

        val userAuthDetails = UserAuthDetails(user)

        return UsernamePasswordAuthenticationToken(userAuthDetails, jwt, listOf())
    }
}
