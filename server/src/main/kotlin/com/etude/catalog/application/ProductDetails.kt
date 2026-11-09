package com.etude.catalog.application

import com.etude.catalog.domain.Product
import com.etude.catalog.domain.ProductStatus
import com.etude.shared.Money
import java.util.UUID

data class ProductDetails(
    val id: UUID,
    val name: String,
    val price: Money,
    val status: ProductStatus,
) {
    companion object {
        fun from(product: Product) = ProductDetails(
            id = requireNotNull(product.id),
            name = product.name,
            price = product.price,
            status = product.status,
        )
    }
}
