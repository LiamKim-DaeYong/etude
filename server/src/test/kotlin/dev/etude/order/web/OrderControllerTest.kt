package dev.etude.order.web

import dev.etude.TestcontainersConfiguration
import dev.etude.catalog.application.ProductService
import dev.etude.shared.Money
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResult
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest(
    @Autowired private val mvc: MockMvcTester,
    @Autowired private val productService: ProductService,
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {

    private val customerId = UUID.randomUUID()

    @Test
    fun `places an order and reads it back`() {
        val keyboard = product("Keyboard", "10000", "KRW")
        val mouse = product("Mouse", "2500", "KRW")

        val created = place(
            lines = """[{"productId":"$keyboard","quantity":2},{"productId":"$mouse","quantity":1}]""",
            expectedTotal = "22500",
        )

        assertThat(created).hasStatus(HttpStatus.CREATED)
        val location = requireNotNull(created.response.getHeader("Location"))

        assertThat(mvc.get().uri(location).exchange())
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo(
                """
                {
                  "customerId": "$customerId",
                  "status": "PLACED",
                  "totalAmount": 22500,
                  "currencyCode": "KRW",
                  "lines": [
                    {"lineNo": 1, "productId": "$keyboard", "productName": "Keyboard", "quantity": 2,
                     "unitPriceAmount": 10000, "amount": 20000, "status": "ORDERED"},
                    {"lineNo": 2, "productId": "$mouse", "productName": "Mouse", "quantity": 1,
                     "unitPriceAmount": 2500, "amount": 2500, "status": "ORDERED"}
                  ]
                }
                """.trimIndent(),
            )
    }

    @Test
    fun `returns the same order time when placing and reading an order`() {
        val keyboard = product("Keyboard", "10000", "KRW")
        val created = place(lines = line(keyboard), expectedTotal = "10000")
        val read = mvc.get().uri(requireNotNull(created.response.getHeader("Location"))).exchange()

        assertThat(orderedAt(read)).isEqualTo(orderedAt(created))
    }

    private fun orderedAt(result: MvcTestResult): String = Regex("\"orderedAt\":\"([^\"]+)\"").find(result.response.contentAsString)!!.groupValues[1]

    @Test
    fun `returns 400 problem for an unknown product`() {
        val unknown = UUID.randomUUID()

        assertProblem(place(lines = line(unknown), expectedTotal = "1000"), HttpStatus.BAD_REQUEST)
            .isLenientlyEqualTo("""{"code":"PRODUCT_NOT_FOUND","detail":"Product not found: $unknown"}""")
    }

    @Test
    fun `returns 409 problem for a product that is not on sale`() {
        val keyboard = product("Keyboard", "10000", "KRW")
        jdbcTemplate.update("update product set sales_status = 'SUSPENDED' where id = ?", keyboard)

        assertProblem(place(lines = line(keyboard), expectedTotal = "10000"), HttpStatus.CONFLICT)
            .isLenientlyEqualTo("""{"code":"PRODUCT_NOT_ORDERABLE"}""")
    }

    @Test
    fun `returns 409 problem for products in different currencies`() {
        val keyboard = product("Keyboard", "10000", "KRW")
        val cable = product("Cable", "9.99", "USD")

        assertProblem(
            place(
                lines = """[{"productId":"$keyboard","quantity":1},{"productId":"$cable","quantity":1}]""",
                expectedTotal = "10000",
            ),
            HttpStatus.CONFLICT,
        ).isLenientlyEqualTo("""{"code":"CURRENCY_MISMATCH"}""")
    }

    @Test
    fun `returns 409 problem with the current total when the total has changed`() {
        val keyboard = product("Keyboard", "10000", "KRW")

        assertProblem(place(lines = line(keyboard, quantity = 2), expectedTotal = "18000"), HttpStatus.CONFLICT)
            .isLenientlyEqualTo("""{"code":"TOTAL_AMOUNT_MISMATCH","currentTotalAmount":20000}""")
    }

    @Test
    fun `returns 400 problem when the expected total has more decimals than the currency allows`() {
        val keyboard = product("Keyboard", "10000", "KRW")

        assertProblem(place(lines = line(keyboard), expectedTotal = "10000.4"), HttpStatus.BAD_REQUEST)
            .isLenientlyEqualTo("""{"detail":"Expected total amount has more decimal places than KRW allows: 10000.4"}""")
    }

    @Test
    fun `accepts an expected total with trailing zeros`() {
        val keyboard = product("Keyboard", "10000", "KRW")

        assertThat(place(lines = line(keyboard), expectedTotal = "10000.00")).hasStatus(HttpStatus.CREATED)
    }

    @Test
    fun `returns 400 problem when a product appears in two lines`() {
        val keyboard = product("Keyboard", "10000", "KRW")

        assertProblem(
            place(
                lines = """[{"productId":"$keyboard","quantity":1},{"productId":"$keyboard","quantity":1}]""",
                expectedTotal = "20000",
            ),
            HttpStatus.BAD_REQUEST,
        )
    }

    @Test
    fun `returns 400 problem for an order without lines`() {
        assertProblem(place(lines = "[]", expectedTotal = "0"), HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `returns 400 problem for a non-positive quantity`() {
        val keyboard = product("Keyboard", "10000", "KRW")

        assertProblem(place(lines = line(keyboard, quantity = 0), expectedTotal = "0"), HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `returns 404 problem for an unknown order`() {
        val id = UUID.randomUUID()

        assertProblem(mvc.get().uri("/api/v1/orders/{id}", id).exchange(), HttpStatus.NOT_FOUND)
            .isLenientlyEqualTo("""{"detail":"Order not found: $id"}""")
    }

    private fun product(name: String, price: String, currency: String): UUID = productService.register(name, Money.of(BigDecimal(price), Currency.getInstance(currency))).id

    private fun line(productId: UUID, quantity: Int = 1) = """[{"productId":"$productId","quantity":$quantity}]"""

    private fun place(lines: String, expectedTotal: String, currency: String = "KRW"): MvcTestResult = mvc.post()
        .uri("/api/v1/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            """
            {"customerId":"$customerId","lines":$lines,"expectedTotalAmount":$expectedTotal,"currencyCode":"$currency"}
            """.trimIndent(),
        )
        .exchange()

    private fun assertProblem(result: MvcTestResult, status: HttpStatus) = assertThat(result)
        .hasStatus(status)
        .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .bodyJson()
        .isLenientlyEqualTo("""{"status":${status.value()},"title":"${status.reasonPhrase}"}""")
}
