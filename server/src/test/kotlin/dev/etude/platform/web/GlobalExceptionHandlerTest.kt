package dev.etude.platform.web

import dev.etude.TestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Import(TestcontainersConfiguration::class, GlobalExceptionHandlerTest.FailingController::class)
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest(
    @Autowired private val mvc: MockMvcTester,
) {

    @RestController
    class FailingController {
        @GetMapping("/test/unexpected-failure")
        fun fail(): String = throw IllegalStateException("internal detail that must not leak")
    }

    @Test
    fun `lists violated fields for an invalid request`() {
        val productId = UUID.randomUUID()
        val result = postJson(
            "/api/v1/orders",
            """
            {"customerId":"${UUID.randomUUID()}",
             "lines":[{"productId":"$productId","quantity":0},{"productId":"$productId","quantity":1}],
             "expectedTotal":{"amount":"1000","currency":"KRW"}}
            """.trimIndent(),
        )

        assertProblem(result, HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
            .isLenientlyEqualTo(
                """
                {"detail":"Request validation failed",
                 "errors":[{"field":"hasDistinctProducts","code":"AssertTrue"},{"field":"lines[0].quantity","code":"Positive"}]}
                """.trimIndent(),
            )
    }

    @Test
    fun `keeps validation messages in English regardless of the requested language`() {
        val result = mvc.post().uri("/admin/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept-Language", "ja")
            .content("""{"name":" ","price":{"amount":"1000","currency":"KRW"}}""")
            .exchange()

        assertProblem(result, HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
            .isLenientlyEqualTo("""{"errors":[{"field":"name","code":"NotBlank","message":"must not be blank"}]}""")
    }

    @Test
    fun `points to the field that could not be read`() {
        val result = postJson(
            "/api/v1/orders",
            """
            {"customerId":"${UUID.randomUUID()}","lines":[{"productId":"${UUID.randomUUID()}","quantity":"many"}],
             "expectedTotal":{"amount":"1000","currency":"KRW"}}
            """.trimIndent(),
        )

        assertProblem(result, HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
            .isLenientlyEqualTo("""{"errors":[{"field":"lines[0].quantity","code":"InvalidValue"}]}""")
    }

    @Test
    fun `returns INVALID_REQUEST for malformed json`() {
        assertProblem(postJson("/api/v1/orders", """{"customerId":"""), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
    }

    @Test
    fun `returns INVALID_REQUEST for a malformed path variable`() {
        assertProblem(mvc.get().uri("/api/v1/orders/not-a-uuid").exchange(), HttpStatus.BAD_REQUEST, "INVALID_REQUEST")
    }

    @Test
    fun `returns NOT_FOUND for a missing resource`() {
        assertProblem(mvc.get().uri("/api/v1/orders/{id}", UUID.randomUUID()).exchange(), HttpStatus.NOT_FOUND, "NOT_FOUND")
    }

    @Test
    fun `returns NOT_FOUND for an unknown path`() {
        assertProblem(mvc.get().uri("/api/v1/unknown").exchange(), HttpStatus.NOT_FOUND, "NOT_FOUND")
    }

    @Test
    fun `returns METHOD_NOT_ALLOWED for an unsupported method`() {
        assertProblem(mvc.delete().uri("/api/v1/orders").exchange(), HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED")
    }

    @Test
    fun `hides internal details of an unexpected failure`() {
        val result = mvc.get().uri("/test/unexpected-failure").exchange()

        assertProblem(result, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR")
            .isLenientlyEqualTo("""{"detail":"An unexpected error occurred"}""")
        assertThat(result.response.contentAsString).doesNotContain("internal detail")
    }

    private fun postJson(uri: String, body: String): MvcTestResult = mvc.post().uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
        .exchange()

    private fun assertProblem(result: MvcTestResult, status: HttpStatus, code: String) = assertThat(result)
        .hasStatus(status)
        .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .bodyJson()
        .isLenientlyEqualTo("""{"status":${status.value()},"title":"${status.reasonPhrase}","code":"$code"}""")
}
