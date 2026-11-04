package dev.etude.catalog.domain

import dev.etude.shared.BaseEntity
import dev.etude.shared.Money
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.math.BigDecimal
import java.util.Currency

@Entity
class Product(
    name: String,
    price: Money,
) : BaseEntity() {

    @Column(nullable = false, length = 200)
    var name: String = name
        protected set

    @Column(name = "price_amount", nullable = false, precision = 19, scale = 4)
    private var priceAmount: BigDecimal = price.amount

    @Column(name = "price_currency", nullable = false, length = 3)
    private var priceCurrency: String = price.currency.currencyCode

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var salesStatus: SalesStatus = SalesStatus.ON_SALE
        protected set

    val price: Money
        get() = Money.of(priceAmount, Currency.getInstance(priceCurrency))

    fun isOrderable(): Boolean = salesStatus == SalesStatus.ON_SALE
}
