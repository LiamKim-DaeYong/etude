package com.etude.platform.web

import io.swagger.v3.oas.annotations.media.Schema

/**
 * API 문서용 오류 본문 형식(RFC 9457 + `code`, `errors`). 실제 응답은 Spring `ProblemDetail`이다.
 */
@Schema(name = "Problem")
data class ProblemBody(
    val type: String?,
    val title: String,
    val status: Int,
    val detail: String?,
    val instance: String?,
    @field:Schema(description = "Error code to branch on", example = "INVALID_REQUEST")
    val code: String,
    @field:Schema(description = "Field errors of an INVALID_REQUEST")
    val errors: List<GlobalExceptionHandler.FieldViolation>?,
)
