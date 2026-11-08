package dev.etude.order.application

import dev.etude.catalog.ProductCatalog
import dev.etude.inventory.InventoryReservation
import dev.etude.inventory.ReservationLine
import dev.etude.order.application.OrderRejectedException.Reason
import dev.etude.order.domain.Order
import dev.etude.order.domain.OrderLine
import dev.etude.order.domain.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val productCatalog: ProductCatalog,
    private val inventoryReservation: InventoryReservation,
    private val clock: Clock,
) {

    @Transactional
    fun place(command: PlaceOrderCommand): OrderDetails {
        val lines = command.lines.map { line ->
            val product = productCatalog.find(line.productId)
                ?: throw OrderRejectedException(Reason.PRODUCT_NOT_FOUND, "Product not found: ${line.productId}")
            if (!product.orderable) {
                throw OrderRejectedException(Reason.PRODUCT_NOT_ORDERABLE, "Product is not orderable: ${product.id}")
            }
            OrderLine(product.id, product.name, line.quantity, product.price)
        }
        if (lines.map { it.unitPrice.currency }.distinct().size > 1) {
            throw OrderRejectedException(Reason.CURRENCY_MISMATCH, "All products in an order must use the same currency")
        }

        val order = Order(command.customerId, lines, Instant.now(clock))
        if (order.total != command.expectedTotal) {
            throw OrderRejectedException(
                Reason.TOTAL_MISMATCH,
                "Total changed: expected ${command.expectedTotal}, current ${order.total}",
                currentTotal = order.total,
            )
        }

        inventoryReservation.reserve(command.lines.map { ReservationLine(it.productId, it.quantity) })
        return OrderDetails.from(orderRepository.save(order))
    }

    fun findOrder(id: UUID): OrderDetails? = orderRepository.findById(id)?.let(OrderDetails::from)
}
