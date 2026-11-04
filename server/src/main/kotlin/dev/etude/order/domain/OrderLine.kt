package dev.etude.order.domain

import dev.etude.shared.BaseEntity
import dev.etude.shared.Money
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

@Entity
@Table(name = "order_line")
class OrderLine(
    productId: UUID,
    productName: String,
    quantity: Int,
    unitPrice: Money,
) : BaseEntity() {

    @Column(nullable = false)
    var lineNo: Int = 0
        protected set

    @Column(nullable = false)
    var productId: UUID = productId
        protected set

    @Column(nullable = false, length = 200)
    var productName: String = productName
        protected set

    @Column(nullable = false)
    var quantity: Int = quantity
        protected set

    @Column(name = "unit_price_amount", nullable = false, precision = 19, scale = 4)
    private var unitPriceAmount: BigDecimal = unitPrice.amount

    @Column(name = "currency_code", nullable = false, length = 3)
    private var currencyCode: String = unitPrice.currency.currencyCode

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderLineStatus = OrderLineStatus.ORDERED
        protected set

    init {
        require(quantity > 0) { "Quantity must be positive: $quantity" }
    }

    val unitPrice: Money
        get() = Money.of(unitPriceAmount, Currency.getInstance(currencyCode))

    val amount: Money
        get() = unitPrice * quantity

    internal fun assignLineNo(lineNo: Int) {
        this.lineNo = lineNo
    }
}
