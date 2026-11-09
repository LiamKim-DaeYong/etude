package com.etude.inventory.web

import com.etude.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResult
import java.util.UUID

@IntegrationTest
class InventoryAdminControllerTest(
    @Autowired private val mvc: MockMvcTester,
) {

    @Test
    fun `registers inventory and reads it back`() {
        val productId = registerProduct()

        val created = register("""{"productId":"$productId","availableQuantity":100}""")

        assertThat(created).hasStatus(HttpStatus.CREATED)
        val location = requireNotNull(created.response.getHeader("Location"))
        assertThat(location).isEqualTo("/admin/v1/inventory-items/$productId")

        assertThat(mvc.get().uri(location).exchange())
            .hasStatusOk()
            .bodyJson()
            .isLenientlyEqualTo("""{"productId":"$productId","availableQuantity":100}""")
    }

    @Test
    fun `registers zero available quantity`() {
        val productId = registerProduct()

        assertThat(register("""{"productId":"$productId","availableQuantity":0}""")).hasStatus(HttpStatus.CREATED)
    }

    @Test
    fun `returns 409 problem when inventory is already registered`() {
        val productId = registerProduct()
        register("""{"productId":"$productId","availableQuantity":10}""")

        assertProblem(register("""{"productId":"$productId","availableQuantity":20}"""), HttpStatus.CONFLICT)
            .isLenientlyEqualTo("""{"code":"INVENTORY_ITEM_ALREADY_REGISTERED"}""")
    }

    @Test
    fun `returns 400 problem for an unknown product`() {
        assertProblem(register("""{"productId":"${UUID.randomUUID()}","availableQuantity":10}"""), HttpStatus.BAD_REQUEST)
            .isLenientlyEqualTo("""{"code":"PRODUCT_NOT_FOUND"}""")
    }

    @Test
    fun `returns 400 problem for a negative available quantity`() {
        val productId = registerProduct()

        assertProblem(register("""{"productId":"$productId","availableQuantity":-1}"""), HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `returns 400 problem for a missing available quantity`() {
        val productId = registerProduct()

        assertProblem(register("""{"productId":"$productId"}"""), HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `returns 404 problem for unregistered inventory`() {
        val productId = UUID.randomUUID()

        assertProblem(mvc.get().uri("/admin/v1/inventory-items/{productId}", productId).exchange(), HttpStatus.NOT_FOUND)
            .isLenientlyEqualTo("""{"detail":"Inventory item not found: $productId"}""")
    }

    private fun registerProduct(): UUID {
        val result = mvc.post().uri("/admin/v1/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Sneakers","price":{"amount":"10000","currency":"KRW"}}""")
            .exchange()
        val location = requireNotNull(result.response.getHeader("Location"))
        return UUID.fromString(location.substringAfterLast('/'))
    }

    private fun register(body: String): MvcTestResult = mvc.post().uri("/admin/v1/inventory-items")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
        .exchange()

    private fun assertProblem(result: MvcTestResult, status: HttpStatus) = assertThat(result)
        .hasStatus(status)
        .hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
        .bodyJson()
        .isLenientlyEqualTo("""{"status":${status.value()},"title":"${status.reasonPhrase}"}""")
}
