package dev.etude.order.application

import dev.etude.shared.Money
import java.util.UUID

data class PlaceOrderCommand(
    val customerId: UUID,
    val lines: List<Line>,
    val expectedTotal: Money,
) {
    data class Line(
        val productId: UUID,
        val quantity: Int,
    )
}
