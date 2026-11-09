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
     * 트랜잭션이 끝날 때까지 행을 잠가 같은 상품의 동시 예약을 하나씩 처리한다.
     * 끝없이 기다리지 않고 [LOCK_TIMEOUT_MILLIS] 뒤에 포기한다(ADR-0003).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MILLIS))
    @Query("select i from InventoryItem i where i.productId = :productId")
    fun findByProductIdForUpdate(productId: UUID): InventoryItem?
}
