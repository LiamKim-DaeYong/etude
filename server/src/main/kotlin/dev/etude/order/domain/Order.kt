package dev.etude.order.domain

import dev.etude.shared.Money
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency
import java.util.UUID

@Entity
@Table(name = "orders")
class Order(
    customerId: UUID,
    lines: List<OrderLine>,
    orderedAt: Instant,
) {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    var id: UUID? = null
        protected set

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
    var status: OrderStatus = OrderStatus.CREATED
        protected set

    @Column(nullable = false)
    var orderedAt: Instant = orderedAt
        protected set

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private var totalAmount: BigDecimal

    @Column(name = "total_currency", nullable = false, length = 3)
    private var totalCurrency: String

    init {
        require(lines.isNotEmpty()) { "Order must have at least one line" }
        lines.forEachIndexed { index, line -> line.assignLineNo(index + 1) }
        val total = lines.map(OrderLine::amount).reduce(Money::plus)
        totalAmount = total.amount
        totalCurrency = total.currency.currencyCode
    }

    val total: Money
        get() = Money.of(totalAmount, Currency.getInstance(totalCurrency))
}
