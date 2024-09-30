package me.dafnik.angular_todos_backend

data class User(
    val id: Long,
    val email: String,
    val name: String,
    val password: String
)
