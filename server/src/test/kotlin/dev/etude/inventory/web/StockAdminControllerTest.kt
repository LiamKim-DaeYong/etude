package dev.etude.inventory.web

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
import java.util.UUID

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@AutoConfigureMockMvc
class StockAdminControllerTest(
    @Autowired private val mvc: MockMvcTester,
) {

    @Test
    fun `registers stock and reads it back`() {
        val productId = registerProduct()

        val created = register("""{"productId":"$productId","quantity":100}""")

        assertThat(created).hasStatus(HttpStatus.CREATED)
        val location = requireNotNull(created.response.getHeader("Location"))
        assertThat(location).isEqualTo("/admin/v1/stocks/$productId")

        assertThat(mvc.get().uri(location).exchange())
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""{"productId":"$productId","quantity":100}""")
    }

    @Test
    fun `registers zero stock`() {
        val productId = registerProduct()

        assertThat(register("""{"productId":"$productId","quantity":0}""")).hasStatus(HttpStatus.CREATED)
    }

    @Test
    fun `returns 409 problem when stock is already registered`() {
        val productId = registerProduct()
        register("""{"productId":"$productId","quantity":10}""")

        assertProblem(register("""{"productId":"$productId","quantity":20}"""), HttpStatus.CONFLICT)
            .isLenientlyEqualTo("""{"code":"STOCK_ALREADY_REGISTERED"}""")
    }

    @Test
    fun `returns 400 problem for an unknown product`() {
        assertProblem(register("""{"productId":"${UUID.randomUUID()}","quantity":10}"""), HttpStatus.BAD_REQUEST)
            .isLenientlyEqualTo("""{"code":"PRODUCT_NOT_FOUND"}""")
    }

    @Test
    fun `returns 400 problem for a negative quantity`() {
        val productId = registerProduct()

        assertProblem(register("""{"productId":"$productId","quantity":-1}"""), HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `returns 400 problem for a missing quantity`() {
        val productId = registerProduct()

        assertProblem(register("""{"productId":"$productId"}"""), HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `returns 404 problem for unregistered stock`() {
        val productId = UUID.randomUUID()

        assertProblem(mvc.get().uri("/admin/v1/stocks/{productId}", productId).exchange(), HttpStatus.NOT_FOUND)
            .isLenientlyEqualTo("""{"detail":"Stock not found: $productId"}""")
    }

    private fun registerProduct(): UUID {
        val result = mvc.post().uri("/admin/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Sneakers","priceAmount":10000,"priceCurrency":"KRW"}""")
            .exchange()
        val location = requireNotNull(result.response.getHeader("Location"))
        return UUID.fromString(location.substringAfterLast('/'))
    }

    private fun register(body: String): MvcTestResult = mvc.post().uri("/admin/v1/stocks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
        .exchange()

    private fun assertProblem(result: MvcTestResult, status: HttpStatus) = assertThat(result)
        .hasStatus(status)
        .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .bodyJson()
        .isLenientlyEqualTo("""{"status":${status.value()},"title":"${status.reasonPhrase}"}""")
}
