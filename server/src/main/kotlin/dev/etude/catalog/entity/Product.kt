package dev.etude.catalog.entity

import dev.etude.shared.Money
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID

@Entity
class Product(
    name: String,
    price: Money,
) {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    var id: UUID? = null
        protected set

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
}

enum class SalesStatus {
    ON_SALE,
    SUSPENDED,
}
