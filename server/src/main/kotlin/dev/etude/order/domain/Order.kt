package dev.etude.order.domain

import dev.etude.shared.BaseEntity
import dev.etude.shared.Money
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
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency
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

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private var totalAmount: BigDecimal

    @Column(name = "currency_code", nullable = false, length = 3)
    private var currencyCode: String

    init {
        require(lines.isNotEmpty()) { "Order must have at least one line" }
        require(lines.map { it.unitPrice.currency }.distinct().size == 1) {
            "All order lines must use the same currency"
        }
        lines.forEachIndexed { index, line -> line.assignLineNo(index + 1) }
        val total = lines.map(OrderLine::amount).reduce(Money::plus)
        totalAmount = total.amount
        currencyCode = total.currency.currencyCode
    }

    val total: Money
        get() = Money.of(totalAmount, Currency.getInstance(currencyCode))
}
