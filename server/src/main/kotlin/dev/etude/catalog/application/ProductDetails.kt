package dev.etude.catalog.application

import dev.etude.catalog.domain.Product
import dev.etude.catalog.domain.ProductStatus
import dev.etude.shared.Money
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
