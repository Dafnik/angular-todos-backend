package me.dafnik.angular_todos_backend

import jakarta.validation.constraints.NotBlank

data class UserLoginDto(
    @get:NotBlank val email: String,
    @get:NotBlank val password: String,
)

data class RefreshJwtWithSessionTokenDto(
    @get:NotBlank var refreshToken: String,
)

data class JwtResponse(
    var accessToken: String,
    var refreshToken: String
)