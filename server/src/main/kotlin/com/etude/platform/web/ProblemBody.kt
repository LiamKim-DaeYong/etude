package com.etude.platform.web

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Shape of every error body (RFC 9457 problem details plus `code` and `errors`), for the API documentation.
 * The body itself is a Spring `ProblemDetail`; the contract test checks that it matches this shape.
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
