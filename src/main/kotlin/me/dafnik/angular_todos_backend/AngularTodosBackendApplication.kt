package me.dafnik.angular_todos_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AngularTodosBackendApplication

fun main(args: Array<String>) {
	runApplication<AngularTodosBackendApplication>(*args)
}
