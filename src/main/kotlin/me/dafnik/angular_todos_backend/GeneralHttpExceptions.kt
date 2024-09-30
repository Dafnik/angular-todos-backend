package me.dafnik.angular_todos_backend

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.http.HttpStatus
import kotlin.text.replace
import kotlin.text.uppercase

open class NotFoundException(
    message: String = "Property not found",
    codeName: String? = null,
) : HttpException(
    HttpStatus.NOT_FOUND,
    message,
    codeName
)

open class EntityAlreadyExistsException(
    message: String = "Entity already exists",
    codeName: String? = null
) : HttpException(
    HttpStatus.CONFLICT,
    message,
    codeName
)

open class ForbiddenException(message: String = "Forbidden", codeName: String? = null) : HttpException(
    HttpStatus.FORBIDDEN,
    message,
    codeName
)

open class UnauthorizedException(message: String = "Not authorized", codeName: String? = null) : HttpException(
    HttpStatus.UNAUTHORIZED,
    message,
    codeName
)

open class BadRequestException(message: String = "Bad request", codeName: String? = null) : HttpException(
    HttpStatus.BAD_REQUEST,
    message,
    codeName
)

open class ServiceUnavailableException(
    message: String = "Service unavailable",
    codeName: String? = null
) : HttpException(
    HttpStatus.SERVICE_UNAVAILABLE,
    message,
    codeName
)

@JsonIgnoreProperties("cause", "stackTrace", "suppressed", "localizedMessage", "httpStatus")
abstract class HttpException(
    val httpStatus: HttpStatus,
    override val message: String,
    codeName: String?
) : Exception() {
    val httpCode: Int = httpStatus.value()
    val codeName: String = (codeName ?: httpStatus.name.replace(" ", "_")).uppercase()
}
