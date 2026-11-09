package com.etude.order.application

import com.etude.catalog.ProductCatalog
import com.etude.order.InsufficientInventoryException
import com.etude.order.InventoryReservation
import com.etude.order.OrderCanceled
import com.etude.order.ReservationLine
import com.etude.order.application.OrderRejectedException.Reason
import com.etude.order.domain.Order
import com.etude.order.domain.OrderLine
import com.etude.order.domain.OrderRepository
import org.springframework.context.ApplicationEventPublisher
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
    private val events: ApplicationEventPublisher,
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

        val saved = orderRepository.save(order)

        try {
            inventoryReservation.reserve(
                orderId = requireNotNull(saved.id),
                lines = command.lines.map { ReservationLine(it.productId, it.quantity) },
            )
        } catch (e: InsufficientInventoryException) {
            throw OrderRejectedException(
                Reason.INSUFFICIENT_INVENTORY,
                "Insufficient inventory for product: ${e.productId}",
                productId = e.productId,
            )
        }

        return OrderDetails.from(saved)
    }

    fun findOrder(id: UUID): OrderDetails? = orderRepository.findById(id)?.let(OrderDetails::from)

    /** Returns null when the order does not exist. */
    @Transactional
    fun cancel(id: UUID): OrderDetails? {
        val order = orderRepository.findByIdForUpdate(id) ?: return null
        if (!order.cancelable) {
            throw OrderCancellationRejectedException(
                OrderCancellationRejectedException.Reason.ORDER_NOT_CANCELABLE,
                "Order cannot be canceled in status ${order.status}: $id",
            )
        }
        order.cancel(Instant.now(clock))

        events.publishEvent(OrderCanceled(id))

        return OrderDetails.from(order)
    }
}
