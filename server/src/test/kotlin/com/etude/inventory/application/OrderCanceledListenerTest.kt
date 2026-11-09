package com.etude.inventory.application

import com.etude.IntegrationTest
import com.etude.catalog.application.ProductService
import com.etude.order.OrderCanceled
import com.etude.order.application.OrderService
import com.etude.order.application.PlaceOrderCommand
import com.etude.shared.Money
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.modulith.events.IncompleteEventPublications
import java.math.BigDecimal
import java.time.Duration
import java.util.Currency
import java.util.UUID

@IntegrationTest
class OrderCanceledListenerTest(
    @Autowired private val orderService: OrderService,
    @Autowired private val productService: ProductService,
    @Autowired private val inventoryService: InventoryService,
    @Autowired private val incompletePublications: IncompleteEventPublications,
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {

    @Test
    fun `releases reserved inventory after an order is canceled`() {
        val productId = product(availableQuantity = 10)
        val orderId = placeOrder(productId, quantity = 3)
        assertThat(availableQuantity(productId)).isEqualTo(7)

        orderService.cancel(orderId)

        awaitPublication(orderId, status = "COMPLETED")
        assertThat(availableQuantity(productId)).isEqualTo(10)
    }

    @Test
    fun `keeps the publication incomplete when release fails and releases inventory on resubmission`() {
        val productId = product(availableQuantity = 10)
        val orderId = placeOrder(productId, quantity = 3)
        jdbcTemplate.update("delete from inventory_item where product_id = ?", productId)

        orderService.cancel(orderId)

        awaitPublication(orderId, status = "FAILED")

        inventoryService.register(productId, 0)
        resubmit(orderId)

        awaitPublication(orderId, status = "COMPLETED")
        assertThat(availableQuantity(productId)).isEqualTo(3)
    }

    /**
     * The registry delivers at least once: if the process stops after the listener commits but before the publication
     * is marked completed, the event is delivered again. Simulated here by marking a completed publication failed.
     */
    @Test
    fun `releases inventory only once when the same event is delivered again`() {
        val productId = product(availableQuantity = 10)
        val orderId = placeOrder(productId, quantity = 3)
        orderService.cancel(orderId)
        awaitPublication(orderId, status = "COMPLETED")

        jdbcTemplate.update(
            "update event_publication set status = 'FAILED', completion_date = null where serialized_event like ?",
            "%$orderId%",
        )
        resubmit(orderId)

        awaitPublication(orderId, status = "COMPLETED")
        assertThat(availableQuantity(productId)).isEqualTo(10)
    }

    private fun product(availableQuantity: Int): UUID {
        val productId = productService.register("Keyboard", PRICE).id
        inventoryService.register(productId, availableQuantity)
        return productId
    }

    private fun placeOrder(productId: UUID, quantity: Int): UUID = orderService.place(
        PlaceOrderCommand(
            customerId = UUID.randomUUID(),
            lines = listOf(PlaceOrderCommand.Line(productId, quantity)),
            expectedTotal = PRICE * quantity,
        ),
    ).id

    private fun availableQuantity(productId: UUID) = inventoryService.findInventoryItem(productId)!!.availableQuantity

    private fun resubmit(orderId: UUID) = incompletePublications.resubmitIncompletePublications { (it.event as? OrderCanceled)?.orderId == orderId }

    private fun awaitPublication(orderId: UUID, status: String) = await().atMost(Duration.ofSeconds(10)).untilAsserted {
        assertThat(publicationStatuses(orderId)).containsExactly(status)
    }

    private fun publicationStatuses(orderId: UUID): List<String?> = jdbcTemplate.queryForList(
        "select status from event_publication where serialized_event like ?",
        String::class.java,
        "%$orderId%",
    )

    companion object {
        private val PRICE = Money.of(BigDecimal("10000"), Currency.getInstance("KRW"))
    }
}
