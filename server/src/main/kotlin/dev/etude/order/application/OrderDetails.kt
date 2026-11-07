package dev.etude.order.application

import dev.etude.order.domain.Order
import dev.etude.order.domain.OrderLine
import dev.etude.order.domain.OrderLineStatus
import dev.etude.order.domain.OrderStatus
import dev.etude.shared.Money
import java.time.Instant
import java.util.UUID

data class OrderDetails(
    val id: UUID,
    val customerId: UUID,
    val status: OrderStatus,
    val orderedAt: Instant,
    val total: Money,
    val lines: List<Line>,
) {
    data class Line(
        val id: UUID,
        val lineNo: Int,
        val productId: UUID,
        val productName: String,
        val quantity: Int,
        val unitPrice: Money,
        val lineTotal: Money,
        val status: OrderLineStatus,
    ) {
        companion object {
            fun from(line: OrderLine) = Line(
                id = requireNotNull(line.id),
                lineNo = line.lineNo,
                productId = line.productId,
                productName = line.productName,
                quantity = line.quantity,
                unitPrice = line.unitPrice,
                lineTotal = line.lineTotal,
                status = line.status,
            )
        }
    }

    companion object {
        fun from(order: Order) = OrderDetails(
            id = requireNotNull(order.id),
            customerId = order.customerId,
            status = order.status,
            orderedAt = order.orderedAt,
            total = order.total,
            lines = order.lines.map(Line::from),
        )
    }
}
