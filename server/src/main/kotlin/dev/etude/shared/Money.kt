package dev.etude.shared

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

class Money private constructor(
    val amount: BigDecimal,
    val currency: Currency,
) {

    init {
        require(amount.signum() >= 0) { "Money amount must not be negative: $amount" }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Currency mismatch: $currency, ${other.currency}" }
        return Money(amount + other.amount, currency)
    }

    operator fun times(quantity: Int): Money {
        require(quantity >= 0) { "Quantity must not be negative: $quantity" }
        return Money(amount * quantity.toBigDecimal(), currency)
    }

    override fun equals(other: Any?): Boolean = other is Money && amount == other.amount && currency == other.currency

    override fun hashCode(): Int = 31 * amount.hashCode() + currency.hashCode()

    override fun toString(): String = "$amount ${currency.currencyCode}"

    companion object {
        private val ROUNDING = RoundingMode.HALF_UP

        fun of(amount: BigDecimal, currency: Currency): Money {
            require(currency.defaultFractionDigits >= 0) { "Unsupported currency: $currency" }
            return Money(amount.setScale(currency.defaultFractionDigits, ROUNDING), currency)
        }

        /** Like [of], but rejects an amount with more decimal places than the currency allows instead of rounding it. */
        fun ofExact(amount: BigDecimal, currency: Currency): Money {
            val money = of(amount, currency)
            require(money.amount.compareTo(amount) == 0) {
                "Amount $amount has more decimal places than ${currency.currencyCode} allows"
            }
            return money
        }
    }
}
