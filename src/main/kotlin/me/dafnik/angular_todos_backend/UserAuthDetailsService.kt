package me.dafnik.angular_todos_backend

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

class UserAuthDetailsService(val userService: UserService) :
    UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userService.getByEmail(
            username
        ) ?: throw UnauthorizedException()

        return UserAuthDetails(
            user
        )
    }
}
