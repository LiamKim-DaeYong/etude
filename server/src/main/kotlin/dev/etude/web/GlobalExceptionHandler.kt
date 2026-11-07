package dev.etude.web

import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Makes every API error a ProblemDetail with a `code` property.
 *
 * Module handlers set business codes for their own exceptions and must be ordered before this one,
 * because [handleUnexpected] matches any exception.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val errors = ex.bindingResult.fieldErrors.map { FieldViolation(it.field, it.code, it.defaultMessage) } +
            ex.bindingResult.globalErrors.map { FieldViolation(null, it.code, it.defaultMessage) }
        val problem = ex.body.apply {
            detail = "Request validation failed"
            setProperty("errors", errors)
        }
        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        // The parent builds the ProblemDetail itself when body is null (e.g. ResponseStatusException),
        // so the code is added to the finished response rather than to the incoming body.
        val response = super.handleExceptionInternal(ex, body, headers, statusCode, request)
        val problem = response?.body as? ProblemDetail
        if (problem != null && problem.properties?.containsKey(CODE) != true) {
            problem.setProperty(CODE, codeFor(statusCode))
        }
        return response
    }

    @ExceptionHandler
    fun handleUnexpected(ex: Exception): ProblemDetail {
        log.error("Unexpected error", ex)
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred").apply {
            setProperty(CODE, "INTERNAL_ERROR")
        }
    }

    private fun codeFor(status: HttpStatusCode): String = when (status.value()) {
        400 -> "INVALID_REQUEST"
        404 -> "NOT_FOUND"
        500 -> "INTERNAL_ERROR"
        else -> HttpStatus.resolve(status.value())?.name ?: "HTTP_${status.value()}"
    }

    data class FieldViolation(val field: String?, val code: String?, val message: String?)

    private companion object {
        const val CODE = "code"
    }
}
