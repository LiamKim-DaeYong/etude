package dev.etude.order.domain

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.Repository
import java.util.UUID

interface OrderRepository : Repository<Order, UUID> {

    fun save(order: Order): Order

    @EntityGraph("Order.lines")
    fun findById(id: UUID): Order?
}
