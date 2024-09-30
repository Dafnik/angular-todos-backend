package me.dafnik.angular_todos_backend

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class UserService(val passwordEncoder: PasswordEncoder) {
    val users = mutableListOf<User>()

    init {
        repeat(20) { index ->
            users.add(User(index.toLong(), "test$index@test.at", "Test User $index", passwordEncoder.encode("Passwort1234")))
        }
    }

    fun getByEmail(mail: String): User? = users.find { it.email == mail }
    fun getById(id: Long): User? = users.find { it.id == id }
}