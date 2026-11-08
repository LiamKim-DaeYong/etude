package dev.etude.inventory

import java.util.UUID

interface InventoryReservation {
    fun reserve(lines: List<ReservationLine>)
}

data class ReservationLine(
    val productId: UUID,
    val quantity: Int,
)
