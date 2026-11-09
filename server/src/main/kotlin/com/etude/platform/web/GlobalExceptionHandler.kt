package com.etude.platform.web

import com.etude.platform.RejectedException
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
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import tools.jackson.core.JacksonException

/**
 * 모든 API 오류를 `code`가 있는 ProblemDetail로 바꾼다. 모듈은 예외 처리기를 따로 두지 않는다.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GlobalExceptionHandler(
    private val rejectionStatuses: RejectionStatusRegistry,
) : ResponseEntityExceptionHandler() {

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
        // body가 null이면 부모가 ProblemDetail을 만들므로, 완성된 응답에 code를 넣는다.
        val response = super.handleExceptionInternal(ex, body, headers, statusCode, request)
        val problem = response?.body as? ProblemDetail
        if (problem != null && problem.properties?.containsKey(CODE) != true) {
            problem.setProperty(CODE, codeFor(statusCode))
        }
        return response
    }

    @ExceptionHandler
    fun handleRejected(ex: RejectedException): ProblemDetail = ProblemDetail.forStatusAndDetail(rejectionStatuses.statusOf(ex.reason), ex.message).apply {
        setProperty(CODE, ex.reason.name)
        ex.properties.forEach(::setProperty)
    }

    /** 잠금 대기 시간 초과나 데드락. 다시 시도하면 성공할 수 있다. */
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

    /** 로그인 실패. 없는 사용자와 틀린 비밀번호를 구분하지 않는다. */
    @ExceptionHandler
    fun handleAuthenticationFailure(ex: AuthenticationException): ProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials").apply {
        setProperty(CODE, INVALID_CREDENTIALS)
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
        /** 프레임워크가 만드는 400(검증 실패, 읽을 수 없는 본문)의 코드. */
        const val INVALID_REQUEST = "INVALID_REQUEST"

        const val INVALID_CREDENTIALS = "INVALID_CREDENTIALS"

        private const val CODE = "code"
        private const val SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE"
        private const val RETRY_AFTER_SECONDS = "1"
    }
}
