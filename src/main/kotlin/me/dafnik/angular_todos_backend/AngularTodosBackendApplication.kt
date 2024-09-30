package me.dafnik.angular_todos_backend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.format.DateTimeFormatter
import java.util.TimeZone

@Import(HttpExceptionHandler::class)
@SpringBootApplication
class AngularTodosBackendApplication

fun main(args: Array<String>) {
	TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
	runApplication<AngularTodosBackendApplication>(*args)
}


// Swagger respects jackson object mapper.
@Bean
fun modelResolver(objectMapper: ObjectMapper): ModelResolver {
	return ModelResolver(objectMapper)
}

const val BEARER_AUTH = "bearerAuth"

@Configuration
@OpenAPIDefinition(info = Info(title = "TodosAPI", version = "v1"))
@SecurityScheme(
	name = BEARER_AUTH,
	type = SecuritySchemeType.HTTP,
	bearerFormat = "JWT",
	scheme = "bearer"
)
class OpenApiConfiguration


const val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT)

@Configuration
class JacksonConfiguration {
	@Bean
	fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer? {
		return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
			// deserializers
			builder.deserializers(LocalDateTimeDeserializer(dateTimeFormatter))

			// serializers
			builder.serializers(LocalDateTimeSerializer(dateTimeFormatter))
		}
	}
}

object Routes {
	const val USER_AUTH = "/v1/auth/**"

	private val SWAGGER = buildList {
		add("/docu")
		add("/swagger-ui/**")
		add("/v3/api-docs/**")
	}

	val USER_MATCHER = buildList {
		addAll(SWAGGER)
		add(USER_AUTH)
		add("/v1/secure**")
		add("/v1/app/**")
	}.toTypedArray()

	val USER_UNSECURED = buildList {
		addAll(SWAGGER)
		add(USER_AUTH)
	}
}
