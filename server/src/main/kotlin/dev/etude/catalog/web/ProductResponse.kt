package dev.etude.catalog.web

import dev.etude.catalog.application.ProductDetails
import dev.etude.catalog.domain.SalesStatus
import dev.etude.shared.Money
import java.util.UUID

data class ProductResponse(
    val id: UUID,
    val name: String,
    val price: Money,
    val salesStatus: SalesStatus,
) {
    companion object {
        fun from(product: ProductDetails) = ProductResponse(
            id = product.id,
            name = product.name,
            price = product.price,
            salesStatus = product.salesStatus,
        )
    }
}
