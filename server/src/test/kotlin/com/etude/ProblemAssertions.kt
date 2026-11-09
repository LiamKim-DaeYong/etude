package com.etude

import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MvcTestResult

/** Problem 응답인지 확인한다. 돌려받은 값으로 본문을 더 검사할 수 있다. */
fun assertProblem(result: MvcTestResult, status: HttpStatus, code: String? = null) = assertThat(result)
    .hasStatus(status)
    .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
    .bodyJson()
    .isLenientlyEqualTo(
        """{"status":${status.value()},"title":"${status.reasonPhrase}"${code?.let { ""","code":"$it"""" } ?: ""}}""",
    )
