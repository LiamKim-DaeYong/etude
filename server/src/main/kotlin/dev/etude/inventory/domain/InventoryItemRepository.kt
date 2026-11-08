package dev.etude.inventory.domain

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.Repository
import java.util.UUID

interface InventoryItemRepository : Repository<InventoryItem, UUID> {

    fun save(inventoryItem: InventoryItem): InventoryItem

    fun findByProductId(productId: UUID): InventoryItem?

    /**
     * Locks the row until the transaction ends so concurrent reservations of the same product run one at a time.
     * Gives up after [LOCK_TIMEOUT_MILLIS] instead of waiting indefinitely; see ADR-0003.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MILLIS))
    @Query("select i from InventoryItem i where i.productId = :productId")
    fun findByProductIdForUpdate(productId: UUID): InventoryItem?

    companion object {
        const val LOCK_TIMEOUT_MILLIS = "2000"
    }
}
