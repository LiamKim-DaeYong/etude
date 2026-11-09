package com.etude.order.domain

import com.etude.platform.LOCK_TIMEOUT_MILLIS
import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.Repository
import java.util.UUID

interface OrderRepository : Repository<Order, UUID> {

    fun save(order: Order): Order

    @EntityGraph("Order.lines")
    fun findById(id: UUID): Order?

    /**
     * 트랜잭션이 끝날 때까지 주문 행을 잠가 같은 주문의 동시 상태 변경을 하나씩 처리한다.
     * 주문 항목은 주문을 통해서만 바뀌므로 잠그지 않는다. [LOCK_TIMEOUT_MILLIS] 뒤에 포기한다(ADR-0003).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MILLIS))
    @Query("select o from Order o where o.id = :id")
    fun findByIdForUpdate(id: UUID): Order?
}
