package dev.etude.catalog.web

import dev.etude.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResult
import java.util.UUID

@IntegrationTest
class ProductControllerTest(
    @Autowired private val mvc: MockMvcTester,
) {

    @Test
    fun `registers a product and reads it back`() {
        val created = register("""{"name":"Keyboard","price":{"amount":"10000","currency":"KRW"}}""")

        assertThat(created).hasStatus(HttpStatus.CREATED)
        val location = requireNotNull(created.response.getHeader("Location"))

        assertThat(mvc.get().uri(location).exchange())
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(
                """{"name":"Keyboard","price":{"amount":"10000","currency":"KRW"},"salesStatus":"ON_SALE"}""",
            )
    }

    @Test
    fun `returns the amount as a string with the currency's minor units`() {
        val created = register("""{"name":"Cable","price":{"amount":"12.5","currency":"USD"}}""")

        assertThat(created)
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .isLenientlyEqualTo("""{"price":{"amount":"12.50","currency":"USD"}}""")
    }

    @Test
    fun `returns 404 problem for an unknown product`() {
        val id = UUID.randomUUID()

        assertProblem(mvc.get().uri("/api/v1/products/{id}", id).exchange(), HttpStatus.NOT_FOUND)
            .isLenientlyEqualTo("""{"detail":"Product not found: $id"}""")
    }

    @Test
    fun `returns 400 problem for a malformed id`() {
        assertProblem(mvc.get().uri("/api/v1/products/not-a-uuid").exchange(), HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `returns 400 problem for a blank name`() {
        assertProblem(
            register("""{"name":" ","price":{"amount":"1000","currency":"KRW"}}"""),
            HttpStatus.BAD_REQUEST,
        )
    }

    @Test
    fun `returns 400 problem for an unknown currency`() {
        assertProblem(
            register("""{"name":"Keyboard","price":{"amount":1000,"currency":"ABC"}}"""),
            HttpStatus.BAD_REQUEST,
        ).isLenientlyEqualTo(
            """{"code":"INVALID_REQUEST","errors":[{"field":"price","code":"InvalidValue","message":"Unsupported currency: ABC"}]}""",
        )
    }

    @Test
    fun `returns 400 problem instead of rounding a price with too many decimals`() {
        assertProblem(
            register("""{"name":"Keyboard","price":{"amount":"10000.4","currency":"KRW"}}"""),
            HttpStatus.BAD_REQUEST,
        ).isLenientlyEqualTo(
            """
            {"code":"INVALID_REQUEST",
             "errors":[{"field":"price","message":"Amount 10000.4 has more decimal places than KRW allows"}]}
            """.trimIndent(),
        )
    }

    @Test
    fun `returns 400 problem for malformed json`() {
        assertProblem(register("""{"name":"""), HttpStatus.BAD_REQUEST)
    }

    private fun register(body: String): MvcTestResult = mvc.post().uri("/admin/v1/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
        .exchange()

    private fun assertProblem(result: MvcTestResult, status: HttpStatus) = assertThat(result)
        .hasStatus(status)
        .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .bodyJson()
        .isLenientlyEqualTo("""{"status":${status.value()},"title":"${status.reasonPhrase}"}""")
}
