package dev.etude.order.domain

import dev.etude.shared.BaseEntity
import dev.etude.shared.Money
import dev.etude.shared.MoneyType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.CompositeType
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
    var lineNumber: Int = 0
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

    @Suppress("JpaAttributeTypeInspection") // Mapped by MoneyType, which the IDE inspection does not understand
    @CompositeType(MoneyType::class)
    var unitPrice: Money = unitPrice
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderLineStatus = OrderLineStatus.PLACED
        protected set

    init {
        require(quantity > 0) { "Quantity must be positive: $quantity" }
    }

    val lineTotal: Money
        get() = unitPrice * quantity

    internal fun assignLineNumber(lineNumber: Int) {
        this.lineNumber = lineNumber
    }
}
