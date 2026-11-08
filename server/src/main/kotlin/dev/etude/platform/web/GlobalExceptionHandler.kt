package dev.etude.platform.web

import io.swagger.v3.oas.annotations.media.Schema
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import tools.jackson.core.JacksonException

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

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val cause = generateSequence<Throwable>(ex) { it.cause }.filterIsInstance<JacksonException>().firstOrNull()
        val path = cause?.path?.let(::toFieldPath)
        if (cause == null || path.isNullOrEmpty()) {
            return super.handleHttpMessageNotReadable(ex, headers, status, request)
        }
        val problem = ProblemDetail.forStatusAndDetail(status, "Request body could not be read").apply {
            setProperty("errors", listOf(FieldViolation(path, "InvalidValue", cause.originalMessage)))
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

    /** Waited too long for a row lock (or lost a deadlock); the same request may succeed when retried. */
    @ExceptionHandler
    fun handleLockFailure(ex: PessimisticLockingFailureException): ResponseEntity<ProblemDetail> {
        log.warn("Lock not acquired", ex)
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "The resource is busy; retry later").apply {
            setProperty(CODE, SERVICE_UNAVAILABLE)
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .header(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS)
            .body(problem)
    }

    @ExceptionHandler
    fun handleUnexpected(ex: Exception): ProblemDetail {
        log.error("Unexpected error", ex)
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred").apply {
            setProperty(CODE, "INTERNAL_ERROR")
        }
    }

    private fun codeFor(status: HttpStatusCode): String = when (status.value()) {
        400 -> INVALID_REQUEST
        404 -> "NOT_FOUND"
        500 -> "INTERNAL_ERROR"
        else -> HttpStatus.resolve(status.value())?.name ?: "HTTP_${status.value()}"
    }

    private fun toFieldPath(path: List<JacksonException.Reference>): String = path.foldIndexed("") { i, acc, ref ->
        when {
            ref.propertyName != null -> if (i == 0) ref.propertyName else "$acc.${ref.propertyName}"
            ref.index >= 0 -> "$acc[${ref.index}]"
            else -> acc
        }
    }

    @Schema(name = "FieldViolation")
    data class FieldViolation(val field: String?, val code: String?, val message: String?)

    companion object {
        /** Code of every 400 the framework produces (validation, unreadable body); any operation can return it. */
        const val INVALID_REQUEST = "INVALID_REQUEST"

        private const val CODE = "code"
        private const val SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE"
        private const val RETRY_AFTER_SECONDS = "1"
    }
}
