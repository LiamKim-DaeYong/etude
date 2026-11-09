package com.etude.order.web

import com.etude.order.application.PlaceOrderCommand
import com.etude.shared.Money
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import java.util.UUID

data class PlaceOrderRequest(
    val customerId: UUID,
    @field:NotEmpty
    val lines: List<@Valid Line>,
    val expectedTotal: Money,
) {
    @Schema(name = "PlaceOrderLine")
    data class Line(
        val productId: UUID,
        @field:Positive
        val quantity: Int,
    )

    @get:Schema(hidden = true)
    @get:AssertTrue(message = "each product must appear in only one line")
    val hasDistinctProducts: Boolean
        get() = lines.distinctBy { it.productId }.size == lines.size

    fun toCommand(): PlaceOrderCommand = PlaceOrderCommand(
        customerId = customerId,
        lines = lines.map { PlaceOrderCommand.Line(it.productId, it.quantity) },
        expectedTotal = expectedTotal,
    )
}
