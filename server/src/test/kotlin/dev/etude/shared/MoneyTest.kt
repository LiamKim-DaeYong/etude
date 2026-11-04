package dev.etude.shared

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Currency
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class MoneyTest {

    private val krw = Currency.getInstance("KRW")
    private val usd = Currency.getInstance("USD")

    @Test
    fun `rounds to the currency's fraction digits with HALF_UP`() {
        assertEquals(BigDecimal("1001"), Money.of(BigDecimal("1000.5"), krw).amount)
        assertEquals(BigDecimal("1000"), Money.of(BigDecimal("1000.4"), krw).amount)
        assertEquals(BigDecimal("10.01"), Money.of(BigDecimal("10.005"), usd).amount)
    }

    @Test
    fun `is equal regardless of the input scale`() {
        val a = Money.of(BigDecimal("1000"), krw)
        val b = Money.of(BigDecimal("1000.00"), krw)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `is not equal when the currency differs`() {
        assertNotEquals(Money.of(BigDecimal("1000"), krw), Money.of(BigDecimal("1000"), usd))
    }

    @Test
    fun `rejects a negative amount`() {
        assertFailsWith<IllegalArgumentException> { Money.of(BigDecimal("-1"), krw) }
    }

    @Test
    fun `allows zero`() {
        assertEquals(BigDecimal("0"), Money.of(BigDecimal.ZERO, krw).amount)
    }

    @Test
    fun `adds amounts of the same currency`() {
        val sum = Money.of(BigDecimal("10.10"), usd) + Money.of(BigDecimal("0.95"), usd)

        assertEquals(Money.of(BigDecimal("11.05"), usd), sum)
    }

    @Test
    fun `rejects adding different currencies`() {
        assertFailsWith<IllegalArgumentException> {
            Money.of(BigDecimal("1000"), krw) + Money.of(BigDecimal("1"), usd)
        }
    }

    @Test
    fun `multiplies by a quantity keeping the currency's fraction digits`() {
        val product = Money.of(BigDecimal("10.10"), usd) * 3

        assertEquals(BigDecimal("30.30"), product.amount)
        assertEquals(usd, product.currency)
    }

    @Test
    fun `multiplies by zero`() {
        assertEquals(BigDecimal("0.00"), (Money.of(BigDecimal("10.10"), usd) * 0).amount)
    }

    @Test
    fun `rejects a negative quantity`() {
        assertFailsWith<IllegalArgumentException> { Money.of(BigDecimal("1000"), krw) * -1 }
    }

    @Test
    fun `rejects a currency without fraction digits`() {
        assertFailsWith<IllegalArgumentException> { Money.of(BigDecimal("1"), Currency.getInstance("XAU")) }
    }
}
