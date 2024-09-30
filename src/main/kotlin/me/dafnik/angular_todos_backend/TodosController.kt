package me.dafnik.angular_todos_backend

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.time.Instant

enum class TodoStatus {
    TODO, DONE
}

data class Todo(
    val id: Long,
    val name: String,
    val description: String?,
    val creatorId: Long,
    val createdAt: Instant,
    val status: TodoStatus
)

val todos = mutableListOf<Todo>(Todo(1, "Test", "test", 1, Instant.now(), TodoStatus.TODO))

@RestController
@RequestMapping("/v1/app/todo")
@Tag(name = "Todos API")
class TodosController() {
    @Operation(
        summary = "Get all todos",
        security = [SecurityRequirement(name = BEARER_AUTH)],
    )
    @GetMapping
    fun getAll() = todos
}
