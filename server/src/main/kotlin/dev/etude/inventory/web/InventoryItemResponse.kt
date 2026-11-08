package dev.etude.inventory.web

import dev.etude.inventory.application.InventoryItemDetails
import java.util.UUID

data class InventoryItemResponse(
    val productId: UUID,
    val availableQuantity: Int,
) {
    companion object {
        fun from(inventoryItem: InventoryItemDetails) = InventoryItemResponse(
            productId = inventoryItem.productId,
            availableQuantity = inventoryItem.availableQuantity,
        )
    }
}
