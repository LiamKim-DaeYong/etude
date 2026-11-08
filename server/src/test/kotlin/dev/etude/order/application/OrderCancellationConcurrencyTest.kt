package dev.etude.order.application

import dev.etude.IntegrationTest
import dev.etude.catalog.application.ProductService
import dev.etude.inventory.application.InventoryService
import dev.etude.order.domain.OrderStatus
import dev.etude.shared.Money
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@IntegrationTest
class OrderCancellationConcurrencyTest(
    @Autowired private val orderService: OrderService,
    @Autowired private val productService: ProductService,
    @Autowired private val inventoryService: InventoryService,
) {

    @Test
    fun `cancels an order only once under concurrent cancellations`() {
        val orderId = placeOrder()
        val requests = 10

        val succeeded = AtomicInteger()
        val rejected = AtomicInteger()
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(requests)

        repeat(requests) {
            executor.submit {
                start.await()
                try {
                    orderService.cancel(orderId)
                    succeeded.incrementAndGet()
                } catch (_: OrderCancellationRejectedException) {
                    rejected.incrementAndGet()
                }
            }
        }
        start.countDown()
        executor.shutdown()
        assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue()

        assertThat(succeeded.get()).isEqualTo(1)
        assertThat(rejected.get()).isEqualTo(requests - 1)
        assertThat(orderService.findOrder(orderId)?.status).isEqualTo(OrderStatus.CANCELED)
    }

    private fun placeOrder(): UUID {
        val price = Money.of(BigDecimal("10000"), Currency.getInstance("KRW"))
        val productId = productService.register("Keyboard", price).id
        inventoryService.register(productId, 10)
        return orderService.place(
            PlaceOrderCommand(UUID.randomUUID(), listOf(PlaceOrderCommand.Line(productId, quantity = 1)), expectedTotal = price),
        ).id
    }
}
