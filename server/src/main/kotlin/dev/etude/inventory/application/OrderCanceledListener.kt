package dev.etude.inventory.application

import dev.etude.order.OrderCanceled
import dev.etude.order.ReservationLine
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class OrderCanceledListener(
    private val inventoryService: InventoryService,
) {

    @ApplicationModuleListener
    fun on(event: OrderCanceled) {
        inventoryService.release(event.lines.map { ReservationLine(it.productId, it.quantity) })
    }
}
