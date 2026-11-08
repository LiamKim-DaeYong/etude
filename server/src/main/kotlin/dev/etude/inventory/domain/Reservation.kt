package dev.etude.inventory.domain

import dev.etude.shared.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.util.UUID

@Entity
class Reservation(
    orderId: UUID,
    productId: UUID,
    quantity: Int,
) : BaseEntity() {

    @Column(nullable = false, updatable = false)
    var orderId: UUID = orderId
        protected set

    @Column(nullable = false, updatable = false)
    var productId: UUID = productId
        protected set

    @Column(nullable = false, updatable = false)
    var quantity: Int = quantity
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReservationStatus = ReservationStatus.RESERVED
        protected set

    init {
        require(quantity > 0) { "Reserved quantity must be positive: $quantity" }
    }

    fun release() {
        check(status == ReservationStatus.RESERVED) { "Reservation is already released: $id" }
        status = ReservationStatus.RELEASED
    }
}
