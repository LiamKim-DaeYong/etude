package dev.etude.order.web

import dev.etude.order.application.PlaceOrderCommand
import dev.etude.shared.Money
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

data class PlaceOrderRequest(
    val customerId: UUID,
    @field:NotEmpty
    @field:Valid
    val lines: List<Line>,
    @field:DecimalMin("0")
    val expectedTotalAmount: BigDecimal,
    @field:Size(min = 3, max = 3)
    val currencyCode: String,
) {
    data class Line(
        val productId: UUID,
        @field:Positive
        val quantity: Int,
    )

    @get:AssertTrue(message = "each product must appear in only one line")
    val hasDistinctProducts: Boolean
        get() = lines.distinctBy { it.productId }.size == lines.size

    fun toCommand(): PlaceOrderCommand = PlaceOrderCommand(
        customerId = customerId,
        lines = lines.map { PlaceOrderCommand.Line(it.productId, it.quantity) },
        expectedTotal = toExpectedTotal(),
    )

    private fun toExpectedTotal(): Money {
        val expectedTotal = try {
            Money.of(expectedTotalAmount, Currency.getInstance(currencyCode))
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency: $currencyCode", e)
        }
        if (expectedTotal.amount.compareTo(expectedTotalAmount) != 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Expected total amount has more decimal places than $currencyCode allows: $expectedTotalAmount",
            )
        }
        return expectedTotal
    }
}
