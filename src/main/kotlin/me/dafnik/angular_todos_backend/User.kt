package me.dafnik.angular_todos_backend

data class UserResponse(
   val id: Long,
    val name: String,
) {
    constructor(it: User): this(it.id, it.name)
}

data class User(
    val id: Long,
    val email: String,
    val name: String,
    val password: String
)
