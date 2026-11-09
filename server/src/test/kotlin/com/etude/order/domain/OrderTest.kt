package com.etude.order.domain

import com.etude.shared.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class OrderTest {

    private val krw = Currency.getInstance("KRW")
    private val usd = Currency.getInstance("USD")

    @Test
    fun `numbers lines in the given order and sums the total`() {
        val order = order(line("Keyboard", 2, "10000", krw), line("Mouse", 1, "2500", krw))

        assertEquals(listOf(1, 2), order.lines.map(OrderLine::lineNumber))
        assertEquals(Money.of(BigDecimal("22500"), krw), order.total)
    }

    @Test
    fun `starts as placed with placed lines`() {
        val order = order(line("Keyboard", 1, "10000", krw))

        assertEquals(OrderStatus.PLACED, order.status)
        assertEquals(listOf(OrderLineStatus.PLACED), order.lines.map(OrderLine::status))
    }

    @Test
    fun `computes the line total from the unit price and quantity`() {
        assertEquals(Money.of(BigDecimal("29.97"), usd), line("Cable", 3, "9.99", usd).lineTotal)
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

    @Test
    fun `cancels a placed order with its lines`() {
        val order = order(line("Keyboard", 1, "10000", krw), line("Mouse", 1, "2500", krw))
        val canceledAt = Instant.parse("2026-11-08T01:00:00Z")

        order.cancel(canceledAt)

        assertEquals(OrderStatus.CANCELED, order.status)
        assertEquals(canceledAt, order.canceledAt)
        assertEquals(listOf(OrderLineStatus.CANCELED, OrderLineStatus.CANCELED), order.lines.map(OrderLine::status))
        assertFalse(order.cancelable)
    }

    @Test
    fun `refuses to cancel an order twice`() {
        val order = order(line("Keyboard", 1, "10000", krw))
        order.cancel(Instant.parse("2026-11-08T01:00:00Z"))

        assertFailsWith<IllegalStateException> { order.cancel(Instant.parse("2026-11-08T02:00:00Z")) }
    }

    private fun order(vararg lines: OrderLine) = Order(UUID.randomUUID(), lines.toList(), Instant.now())

    private fun line(name: String, quantity: Int, price: String, currency: Currency) = OrderLine(UUID.randomUUID(), name, quantity, Money.of(BigDecimal(price), currency))
}
