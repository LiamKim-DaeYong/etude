package com.etude.inventory.application

import com.etude.IntegrationTest
import com.etude.catalog.application.ProductService
import com.etude.order.InsufficientInventoryException
import com.etude.order.ReservationLine
import com.etude.platform.LOCK_TIMEOUT_MILLIS
import com.etude.shared.Money
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.CannotAcquireLockException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import java.time.Duration
import java.util.Currency
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

@IntegrationTest
class InventoryReservationConcurrencyTest(
    @Autowired private val productService: ProductService,
    @Autowired private val inventoryService: InventoryService,
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired private val transactionTemplate: TransactionTemplate,
) {

    @Test
    fun `reserves no more than the available quantity under concurrent orders`() {
        val initialQuantity = 10
        val requests = 100
        val productId = product(availableQuantity = initialQuantity)

        val succeeded = AtomicInteger()
        val rejected = AtomicInteger()
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(32)

        repeat(requests) {
            executor.submit {
                start.await()
                try {
                    inventoryService.reserve(UUID.randomUUID(), listOf(ReservationLine(productId, quantity = 1)))
                    succeeded.incrementAndGet()
                } catch (_: InsufficientInventoryException) {
                    rejected.incrementAndGet()
                }
            }
        }
        start.countDown()
        executor.shutdown()
        assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue()

        val remaining = inventoryService.findInventoryItem(productId)!!.availableQuantity

        assertThat(succeeded.get()).isEqualTo(initialQuantity)
        assertThat(remaining).isZero()
        assertThat(reservationCount(productId)).isEqualTo(initialQuantity)
    }

    @Test
    fun `gives up reserving when another transaction holds the inventory item too long`() {
        val productId = product(availableQuantity = 10)
        val locked = CountDownLatch(1)
        val release = CountDownLatch(1)
        val holder = thread {
            transactionTemplate.executeWithoutResult {
                jdbcTemplate.queryForList("select id from inventory_item where product_id = ? for update", productId)
                locked.countDown()
                release.await()
            }
        }

        try {
            assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue()
            val started = System.nanoTime()

            assertThatThrownBy { inventoryService.reserve(UUID.randomUUID(), listOf(ReservationLine(productId, quantity = 1))) }
                .isInstanceOf(CannotAcquireLockException::class.java)
            assertThat(Duration.ofNanos(System.nanoTime() - started))
                .isGreaterThanOrEqualTo(Duration.ofMillis(LOCK_TIMEOUT_MILLIS.toLong()))
                .isLessThan(Duration.ofSeconds(10))
        } finally {
            release.countDown()
            holder.join()
        }
        assertThat(inventoryService.findInventoryItem(productId)!!.availableQuantity).isEqualTo(10)
        assertThat(reservationCount(productId)).isZero()
    }

    private fun reservationCount(productId: UUID) = jdbcTemplate.queryForObject("select count(*) from reservation where product_id = ?", Int::class.java, productId)

    private fun product(availableQuantity: Int) = productService.register("Keyboard", Money.of(BigDecimal("10000"), Currency.getInstance("KRW"))).id
        .also { inventoryService.register(it, availableQuantity) }
}
