package dev.etude.order

import java.util.UUID

interface InventoryReservation {
    fun reserve(orderId: UUID, lines: List<ReservationLine>)
}

data class ReservationLine(
    val productId: UUID,
    val quantity: Int,
)
