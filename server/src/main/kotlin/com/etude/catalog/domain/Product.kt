package com.etude.catalog.domain

import com.etude.shared.BaseEntity
import com.etude.shared.Money
import com.etude.shared.MoneyType
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

    @Suppress("JpaAttributeTypeInspection") // IntelliJ JPA 검사가 @CompositeType을 인식하지 못한다
    @CompositeType(MoneyType::class)
    var price: Money = price
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ProductStatus = ProductStatus.ACTIVE
        protected set

    fun isOrderable(): Boolean = status == ProductStatus.ACTIVE
}
