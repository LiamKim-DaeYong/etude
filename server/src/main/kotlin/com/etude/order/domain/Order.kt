package com.etude.order.domain

import com.etude.shared.BaseEntity
import com.etude.shared.Money
import com.etude.shared.MoneyType
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
    placedAt: Instant,
) : BaseEntity() {

    @Column(nullable = false)
    var customerId: UUID = customerId
        protected set

    @OneToMany(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    @OrderBy("lineNumber")
    private val _lines: MutableList<OrderLine> = lines.toMutableList()

    val lines: List<OrderLine>
        get() = _lines.toList()

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.PLACED
        protected set

    @Column(nullable = false)
    var placedAt: Instant = placedAt
        protected set

    @Column
    var canceledAt: Instant? = null
        protected set

    val cancelable: Boolean
        get() = status == OrderStatus.PLACED

    init {
        require(lines.isNotEmpty()) { "Order must have at least one line" }
        require(lines.map { it.unitPrice.currency }.distinct().size == 1) {
            "All order lines must use the same currency"
        }
        lines.forEachIndexed { index, line -> line.assignLineNumber(index + 1) }
    }

    // 선언 순서대로 초기화되므로 init 블록의 주문 항목 검증 뒤에 계산된다.
    @Suppress("JpaAttributeTypeInspection") // IntelliJ JPA 검사가 @CompositeType을 인식하지 못한다
    @CompositeType(MoneyType::class)
    var total: Money = lines.map(OrderLine::lineTotal).reduce(Money::plus)
        protected set

    fun cancel(canceledAt: Instant) {
        check(cancelable) { "Order cannot be canceled in status $status" }
        status = OrderStatus.CANCELED
        this.canceledAt = canceledAt
        _lines.forEach(OrderLine::cancel)
    }
}
