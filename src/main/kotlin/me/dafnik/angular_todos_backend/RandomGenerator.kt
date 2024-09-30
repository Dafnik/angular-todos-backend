package me.dafnik.angular_todos_backend

import java.security.SecureRandom

val RANDOM_STRING_CHAR_POOL: List<Char> =
    ('a'..'z') + ('A'..'Z') + ('0'..'9') + '-' + '_'

val RANDOM: SecureRandom = SecureRandom()

object RandomGenerator {

    fun string(size: Int = 20): String =
        (1..size)
            .map { RANDOM_STRING_CHAR_POOL[int(size - 1)] }
            .joinToString("")

    fun int( max: Int = 10): Int = RANDOM.nextInt(max + 1)
}
