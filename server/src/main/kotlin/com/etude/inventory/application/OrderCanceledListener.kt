package com.etude.inventory.application

import com.etude.order.OrderCanceled
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class OrderCanceledListener(
    private val inventoryService: InventoryService,
) {

    @ApplicationModuleListener
    fun on(event: OrderCanceled) {
        inventoryService.release(event.orderId)
    }
}
