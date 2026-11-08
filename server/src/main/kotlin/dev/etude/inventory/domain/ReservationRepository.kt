package dev.etude.inventory.domain

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.Repository
import java.util.UUID

interface ReservationRepository : Repository<Reservation, UUID> {

    fun save(reservation: Reservation): Reservation

    /**
     * Locks the order's reservations still to release, so a redelivered event waits and then finds none.
     * Gives up after [InventoryItemRepository.LOCK_TIMEOUT_MILLIS]; see ADR-0003.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(
        QueryHint(
            name = "jakarta.persistence.lock.timeout",
            value = InventoryItemRepository.LOCK_TIMEOUT_MILLIS,
        ),
    )
    @Query("select r from Reservation r where r.orderId = :orderId and r.status = :status order by r.productId")
    fun findByOrderIdAndStatusForUpdate(orderId: UUID, status: ReservationStatus): List<Reservation>
}
