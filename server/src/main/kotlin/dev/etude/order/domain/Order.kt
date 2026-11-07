package dev.etude.order.domain

import dev.etude.shared.BaseEntity
import dev.etude.shared.Money
import dev.etude.shared.MoneyType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.CompositeType
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "orders")
@NamedEntityGraph(name = "Order.lines", attributeNodes = [NamedAttributeNode("_lines")])
class Order(
    customerId: UUID,
    lines: List<OrderLine>,
    orderedAt: Instant,
) : BaseEntity() {

    @Column(nullable = false)
    var customerId: UUID = customerId
        protected set

    @OneToMany(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    @OrderBy("lineNo")
    private val _lines: MutableList<OrderLine> = lines.toMutableList()

    val lines: List<OrderLine>
        get() = _lines.toList()

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.PLACED
        protected set

    @Column(nullable = false)
    var orderedAt: Instant = orderedAt
        protected set

    init {
        require(lines.isNotEmpty()) { "Order must have at least one line" }
        require(lines.map { it.unitPrice.currency }.distinct().size == 1) {
            "All order lines must use the same currency"
        }
        lines.forEachIndexed { index, line -> line.assignLineNo(index + 1) }
    }

    // Declared after the init block: initializers run in declaration order, so the lines are already validated.
    @CompositeType(MoneyType::class)
    var total: Money = lines.map(OrderLine::lineTotal).reduce(Money::plus)
        protected set
}
