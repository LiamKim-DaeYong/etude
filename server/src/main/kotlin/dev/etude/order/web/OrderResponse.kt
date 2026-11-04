package dev.etude.order.web

import dev.etude.order.domain.Order
import dev.etude.order.domain.OrderLine
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val customerId: UUID,
    val status: String,
    val orderedAt: Instant,
    val totalAmount: BigDecimal,
    val currencyCode: String,
    val lines: List<Line>,
) {
    data class Line(
        val id: UUID,
        val lineNo: Int,
        val productId: UUID,
        val productName: String,
        val quantity: Int,
        val unitPriceAmount: BigDecimal,
        val amount: BigDecimal,
        val status: String,
    ) {
        companion object {
            fun from(line: OrderLine) = Line(
                id = requireNotNull(line.id),
                lineNo = line.lineNo,
                productId = line.productId,
                productName = line.productName,
                quantity = line.quantity,
                unitPriceAmount = line.unitPrice.amount,
                amount = line.amount.amount,
                status = line.status.name,
            )
        }
    }

    companion object {
        fun from(order: Order) = OrderResponse(
            id = requireNotNull(order.id),
            customerId = order.customerId,
            status = order.status.name,
            orderedAt = order.orderedAt,
            totalAmount = order.total.amount,
            currencyCode = order.total.currency.currencyCode,
            lines = order.lines.map(Line::from),
        )
    }
}
