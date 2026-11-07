package dev.etude.order.web

import dev.etude.order.application.OrderDetails
import dev.etude.order.domain.OrderLineStatus
import dev.etude.order.domain.OrderStatus
import dev.etude.shared.Money
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val customerId: UUID,
    val status: OrderStatus,
    val orderedAt: Instant,
    val total: Money,
    val lines: List<Line>,
) {
    @Schema(name = "OrderLineResponse")
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
            fun from(line: OrderDetails.Line) = Line(
                id = line.id,
                lineNo = line.lineNo,
                productId = line.productId,
                productName = line.productName,
                quantity = line.quantity,
                unitPrice = line.unitPrice,
                lineTotal = line.amount,
                status = line.status,
            )
        }
    }

    companion object {
        fun from(order: OrderDetails) = OrderResponse(
            id = order.id,
            customerId = order.customerId,
            status = order.status,
            orderedAt = order.orderedAt,
            total = order.total,
            lines = order.lines.map(Line::from),
        )
    }
}
