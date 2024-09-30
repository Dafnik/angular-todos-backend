package me.dafnik.angular_todos_backend


data class ErrorResponse(
    val message: String,
    val code: Int,
    val codeName: String,
) {
    override fun toString(): String {
        return """{"message": "$message", "code": $code, "code_name": "$codeName"}""".trimIndent()
    }
}
