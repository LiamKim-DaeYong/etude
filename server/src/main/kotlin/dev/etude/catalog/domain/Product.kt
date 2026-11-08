package dev.etude.catalog.domain

import dev.etude.shared.BaseEntity
import dev.etude.shared.Money
import dev.etude.shared.MoneyType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.hibernate.annotations.CompositeType

@Entity
class Product(
    name: String,
    price: Money,
) : BaseEntity() {

    @Column(nullable = false, length = 200)
    var name: String = name
        protected set

    @Suppress("JpaAttributeTypeInspection") // Mapped by MoneyType, which the IDE inspection does not understand
    @CompositeType(MoneyType::class)
    var price: Money = price
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ProductStatus = ProductStatus.ACTIVE
        protected set

    fun isOrderable(): Boolean = status == ProductStatus.ACTIVE
}
