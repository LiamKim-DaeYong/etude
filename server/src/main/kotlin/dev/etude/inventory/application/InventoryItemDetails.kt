package dev.etude.inventory.application

import dev.etude.inventory.domain.InventoryItem
import java.util.UUID

data class InventoryItemDetails(
    val productId: UUID,
    val availableQuantity: Int,
) {
    companion object {
        fun from(inventoryItem: InventoryItem) = InventoryItemDetails(
            productId = inventoryItem.productId,
            availableQuantity = inventoryItem.availableQuantity,
        )
    }
}
