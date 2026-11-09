package com.etude.catalog.web

import com.etude.catalog.application.ProductDetails
import com.etude.catalog.domain.ProductStatus
import com.etude.shared.Money
import java.util.UUID

data class ProductResponse(
    val id: UUID,
    val name: String,
    val price: Money,
    val status: ProductStatus,
) {
    companion object {
        fun from(product: ProductDetails) = ProductResponse(
            id = product.id,
            name = product.name,
            price = product.price,
            status = product.status,
        )
    }
}
