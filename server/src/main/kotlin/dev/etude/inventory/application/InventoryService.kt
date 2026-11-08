package dev.etude.inventory.application

import dev.etude.catalog.ProductCatalog
import dev.etude.inventory.InsufficientInventoryException
import dev.etude.inventory.InventoryReservation
import dev.etude.inventory.ReservationLine
import dev.etude.inventory.application.InventoryRejectedException.Reason
import dev.etude.inventory.domain.InventoryItem
import dev.etude.inventory.domain.InventoryItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class InventoryService(
    private val inventoryItemRepository: InventoryItemRepository,
    private val productCatalog: ProductCatalog,
) : InventoryReservation {

    @Transactional
    fun register(productId: UUID, availableQuantity: Int): InventoryItemDetails {
        productCatalog.find(productId)
            ?: throw InventoryRejectedException(Reason.PRODUCT_NOT_FOUND, "Product not found: $productId")

        if (inventoryItemRepository.findByProductId(productId) != null) {
            throw InventoryRejectedException(Reason.INVENTORY_ITEM_ALREADY_REGISTERED, "Inventory item already registered: $productId")
        }

        return InventoryItemDetails.from(inventoryItemRepository.save(InventoryItem(productId, availableQuantity)))
    }

    fun findInventoryItem(productId: UUID): InventoryItemDetails? = inventoryItemRepository.findByProductId(productId)
        ?.let(InventoryItemDetails::from)

    @Transactional
    override fun reserve(lines: List<ReservationLine>) {
        lines.sortedBy { it.productId }.forEach { line ->
            val inventoryItem = inventoryItemRepository.findByProductIdForUpdate(line.productId)
                ?: throw InsufficientInventoryException(line.productId, line.quantity, availableQuantity = 0)
            inventoryItem.reserve(line.quantity)
        }
    }
}
