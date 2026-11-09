package com.etude.platform.web

import io.swagger.v3.oas.annotations.media.Schema

/**
 * API 문서에 쓰는 모든 오류 본문의 모양(RFC 9457 problem details에 `code`, `errors`를 더함).
 * 실제 본문은 Spring `ProblemDetail`이고, 계약 테스트가 이 모양과 맞는지 확인한다.
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
