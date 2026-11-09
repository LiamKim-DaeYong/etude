package dev.etude.order.domain

import dev.etude.platform.LOCK_TIMEOUT_MILLIS
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
     * Locks the order row until the transaction ends so concurrent state changes of the same order run one at a time.
     * Lines are not locked: they change only through their order. Gives up after [LOCK_TIMEOUT_MILLIS]; see ADR-0003.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = LOCK_TIMEOUT_MILLIS))
    @Query("select o from Order o where o.id = :id")
    fun findByIdForUpdate(id: UUID): Order?
}
