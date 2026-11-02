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

    override fun equals(other: Any?): Boolean = other is Money && amount == other.amount && currency == other.currency

    override fun hashCode(): Int = 31 * amount.hashCode() + currency.hashCode()

    override fun toString(): String = "$amount ${currency.currencyCode}"

    companion object {
        private val ROUNDING = RoundingMode.HALF_UP

        fun of(amount: BigDecimal, currency: Currency): Money {
            require(currency.defaultFractionDigits >= 0) { "Unsupported currency: $currency" }
            return Money(amount.setScale(currency.defaultFractionDigits, ROUNDING), currency)
        }
    }
}
