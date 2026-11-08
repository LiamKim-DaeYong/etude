package dev.etude.inventory.domain

import org.springframework.data.repository.Repository
import java.util.UUID

interface InventoryItemRepository : Repository<InventoryItem, UUID> {

    fun save(inventoryItem: InventoryItem): InventoryItem

    fun findByProductId(productId: UUID): InventoryItem?
}
