package dev.etude.order.domain

import dev.etude.shared.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrderTest {

    private val krw = Currency.getInstance("KRW")
    private val usd = Currency.getInstance("USD")

    @Test
    fun `numbers lines in the given order and sums the total`() {
        val order = order(line("Keyboard", 2, "10000", krw), line("Mouse", 1, "2500", krw))

        assertEquals(listOf(1, 2), order.lines.map(OrderLine::lineNo))
        assertEquals(Money.of(BigDecimal("22500"), krw), order.total)
    }

    @Test
    fun `starts as placed with ordered lines`() {
        val order = order(line("Keyboard", 1, "10000", krw))

        assertEquals(OrderStatus.PLACED, order.status)
        assertEquals(listOf(OrderLineStatus.ORDERED), order.lines.map(OrderLine::status))
    }

    @Test
    fun `computes the line amount from the unit price and quantity`() {
        assertEquals(Money.of(BigDecimal("29.97"), usd), line("Cable", 3, "9.99", usd).amount)
    }

    @Test
    fun `rejects an order without lines`() {
        assertFailsWith<IllegalArgumentException> { order() }
    }

    @Test
    fun `rejects lines in different currencies`() {
        assertFailsWith<IllegalArgumentException> {
            order(line("Keyboard", 1, "10000", krw), line("Cable", 1, "9.99", usd))
        }
    }

    @Test
    fun `rejects a non-positive quantity`() {
        assertFailsWith<IllegalArgumentException> { line("Keyboard", 0, "10000", krw) }
    }

    private fun order(vararg lines: OrderLine) = Order(UUID.randomUUID(), lines.toList(), Instant.now())

    private fun line(name: String, quantity: Int, price: String, currency: Currency) = OrderLine(UUID.randomUUID(), name, quantity, Money.of(BigDecimal(price), currency))
}
