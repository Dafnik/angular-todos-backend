package me.dafnik.angular_todos_backend

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.Instant

enum class TodoStatus {
    TODO, DONE
}

data class Todo(
    val id: Long,
    var name: String,
    var description: String?,
    val creatorId: Long,
    val createdAt: Instant,
    var status: TodoStatus
)

data class TodoResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val creator: UserResponse,
    val createdAt: Instant,
    val status: TodoStatus,
) {
    constructor(it: Todo, user: User): this(it.id, it.name, it.description, UserResponse(user), it.createdAt, it.status)
}

data class CreateTodoDto(
    @get:NotBlank @get:Size(min = 2, max = 50) val name: String,
    val description: String?,
)

data class UpdateTodoDto(
    @get:NotNull val id: Long,
    @get:NotBlank @get:Size(min = 2, max = 50) val name: String,
    val description: String?,
    val status: TodoStatus,
)

val todos = mutableListOf<Todo>()

@RestController
@RequestMapping("/v1/app/todo")
@Tag(name = "Todos API")
class TodosController(
    val userService: UserService
) {
    @Operation(
        summary = "Get your todos",
        security = [SecurityRequirement(name = BEARER_AUTH)],
    )
    @GetMapping
    fun getAll(authentication: Authentication): List<TodoResponse> {
        val user = userService.getByAuth(authentication)
        return todos.filter { it.creatorId == user.id }.map { TodoResponse(it, userService.getByIdOrThrow(it.creatorId) ) }
    }

    @Operation(
        summary = "Get table by id",
        security = [SecurityRequirement(name = BEARER_AUTH)],
    )
    @GetMapping("/{id}")
    fun get(authentication: Authentication, @PathVariable("id") id: Long): TodoResponse {
        val todo = todos.find { it.id == id } ?: throw ForbiddenException()
        val user = userService.getByAuth(authentication)
        checkPermission(todo, user)
        return TodoResponse(todo, user)
    }

    @Operation(
        summary = "Create todo",
        security = [SecurityRequirement(name = BEARER_AUTH)],
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(authentication: Authentication, @Valid @RequestBody dto: CreateTodoDto): TodoResponse {
        val user = userService.getByAuth(authentication)
        val todo = Todo(
            id = RandomGenerator.int(1000000000).toLong(),
            name = dto.name,
            description = dto.description,
            creatorId = user.id,
            createdAt = Instant.now(),
            status = TodoStatus.TODO
        )
        todos.add(todo)

        return TodoResponse(todo, user)
    }

    @Operation(
        summary = "Update todo",
        security = [SecurityRequirement(name = BEARER_AUTH)],
    )
    @ResponseStatus(HttpStatus.OK)
    @PutMapping
    fun update(authentication: Authentication, @Valid @RequestBody dto: UpdateTodoDto): TodoResponse {
        val todo = todos.find { it.id == dto.id } ?: throw ForbiddenException()
        val user = userService.getByAuth(authentication)

        checkPermission(todo, user)

        todo.apply {
            this.name = dto.name
            this.description = dto.description
            this.status = dto.status
        }

        return TodoResponse(todo, user)
    }

    @Operation(
        summary = "Delete table",
        security = [SecurityRequirement(name = BEARER_AUTH)],
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    fun delete(authentication: Authentication, @PathVariable("id") id: Long) {
        val todo = todos.find { it.id == id } ?: throw ForbiddenException()
        val user = userService.getByAuth(authentication)

        checkPermission(todo, user)

        todos.remove(todo)
    }

    private fun checkPermission(todo: Todo, user: User) {
        if (todo.creatorId != user.id) {
            throw ForbiddenException()
        }
    }
}
