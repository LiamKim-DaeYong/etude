package com.etude.order.application

import com.etude.shared.Money
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
