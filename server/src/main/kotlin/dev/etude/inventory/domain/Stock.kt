package dev.etude.inventory.domain

import dev.etude.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import java.util.UUID

@Entity
class Stock(
    productId: UUID,
    quantity: Int,
) : BaseEntity() {

    @Column(nullable = false, updatable = false)
    var productId: UUID = productId
        protected set

    @Column(nullable = false)
    var quantity: Int = quantity
        protected set

    init {
        require(quantity >= 0) { "Stock quantity must not be negative: $quantity" }
    }

    fun decrease(amount: Int) {
        require(amount > 0) { "Decrease amount must be positive: $amount" }
        if (amount > quantity) {
            throw InsufficientStockException(productId, requested = amount, available = quantity)
        }
        quantity -= amount
    }
}
