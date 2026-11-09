package com.etude.platform.security

import com.etude.platform.web.OpenApiConfiguration
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.csrf.CsrfException
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

/**
 * 컨트롤러에 닿기 전에 보안 필터가 거절한 요청의 Problem 본문.
 * [com.etude.platform.web.GlobalExceptionHandler]의 본문과 같은 모양이다.
 */
@Component
class SecurityProblemWriter(
    private val jsonMapper: JsonMapper,
) {

    fun authenticationRequired(request: HttpServletRequest, response: HttpServletResponse, ex: AuthenticationException) = write(request, response, HttpStatus.UNAUTHORIZED, AUTHENTICATION_REQUIRED, "Sign in to use this API")

    fun accessDenied(request: HttpServletRequest, response: HttpServletResponse, ex: AccessDeniedException) = when (ex) {
        is CsrfException -> write(request, response, HttpStatus.FORBIDDEN, CSRF_TOKEN_INVALID, "Missing or invalid CSRF token")
        else -> write(request, response, HttpStatus.FORBIDDEN, ACCESS_DENIED, "Not allowed for this user")
    }

    private fun write(request: HttpServletRequest, response: HttpServletResponse, status: HttpStatus, code: String, detail: String) {
        response.status = status.value()
        response.contentType = OpenApiConfiguration.PROBLEM_JSON
        response.characterEncoding = Charsets.UTF_8.name()
        val body = linkedMapOf(
            "type" to "about:blank",
            "title" to status.reasonPhrase,
            "status" to status.value(),
            "detail" to detail,
            "instance" to request.requestURI,
            "code" to code,
        )
        jsonMapper.writeValue(response.outputStream, body)
    }

    companion object {
        const val AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED"
        const val ACCESS_DENIED = "ACCESS_DENIED"
        const val CSRF_TOKEN_INVALID = "CSRF_TOKEN_INVALID"
    }
}
