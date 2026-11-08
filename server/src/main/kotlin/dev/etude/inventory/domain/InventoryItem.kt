package dev.etude.inventory.domain

import dev.etude.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import java.util.UUID

@Entity
class InventoryItem(
    productId: UUID,
    availableQuantity: Int,
) : BaseEntity() {

    @Column(nullable = false, updatable = false)
    var productId: UUID = productId
        protected set

    @Column(nullable = false)
    var availableQuantity: Int = availableQuantity
        protected set

    init {
        require(availableQuantity >= 0) { "Available quantity must not be negative: $availableQuantity" }
    }

    fun reserve(quantity: Int) {
        require(quantity > 0) { "Reserved quantity must be positive: $quantity" }
        if (quantity > availableQuantity) {
            throw InsufficientInventoryException(productId, requested = quantity, available = availableQuantity)
        }
        availableQuantity -= quantity
    }
}
