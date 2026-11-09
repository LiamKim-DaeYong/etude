package com.etude.inventory.domain

import com.etude.platform.LOCK_TIMEOUT_MILLIS
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
     * 재고 행을 잠그고 조회한다. [LOCK_TIMEOUT_MILLIS] 안에 잠그지 못하면 실패한다(ADR-0003).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MILLIS))
    @Query("select i from InventoryItem i where i.productId = :productId")
    fun findByProductIdForUpdate(productId: UUID): InventoryItem?
}
