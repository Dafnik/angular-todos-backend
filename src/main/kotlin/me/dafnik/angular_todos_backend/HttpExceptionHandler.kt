package me.dafnik.angular_todos_backend

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import java.lang.reflect.InvocationTargetException

@ControllerAdvice
class HttpExceptionHandler {
    private final val jsonHeader = HttpHeaders()

    init {
        jsonHeader.contentType = MediaType.APPLICATION_JSON
    }

    @ResponseBody
    @ExceptionHandler(HttpException::class)
    fun handleException(
        servletRequest: HttpServletRequest,
        exception: HttpException
    ): ResponseEntity<HttpException> {
        return ResponseEntity(exception, jsonHeader, exception.httpStatus)
    }


    @ResponseBody
    @ExceptionHandler(InvocationTargetException::class)
    fun handleInvocationTargetException(
        servletRequest: HttpServletRequest,
        exception: InvocationTargetException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                "InvocationTargetException",
                404,
                "invocation_target_exception"
            ),
            jsonHeader,
            HttpStatus.NOT_FOUND
        )
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val error = ex.bindingResult.fieldErrors[0]
        return ResponseEntity(
            ErrorResponse(
                error.defaultMessage!!,
                400,
                "BAD_REQUEST"
            ),
            jsonHeader,
            HttpStatus.BAD_REQUEST
        )
    }

    @ResponseBody
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        servletRequest: HttpServletRequest,
        exception: AccessDeniedException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                "Insufficient permissions",
                403,
                "FORBIDDEN"
            ),
            jsonHeader,
            HttpStatus.FORBIDDEN
        )
    }
}
