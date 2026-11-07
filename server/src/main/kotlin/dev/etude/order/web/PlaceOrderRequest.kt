package dev.etude.order.web

import dev.etude.order.application.PlaceOrderCommand
import dev.etude.shared.Money
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import java.util.UUID

data class PlaceOrderRequest(
    val customerId: UUID,
    @field:NotEmpty
    @field:Valid
    val lines: List<Line>,
    val expectedTotal: Money,
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
        expectedTotal = expectedTotal,
    )
}
