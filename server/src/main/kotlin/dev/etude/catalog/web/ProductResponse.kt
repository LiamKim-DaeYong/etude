package dev.etude.catalog.web

import dev.etude.catalog.application.ProductDetails
import java.util.UUID

data class ProductResponse(
    val id: UUID,
    val name: String,
    val priceAmount: String,
    val priceCurrency: String,
    val salesStatus: String,
) {
    companion object {
        fun from(product: ProductDetails) = ProductResponse(
            id = product.id,
            name = product.name,
            priceAmount = product.price.amount.toPlainString(),
            priceCurrency = product.price.currency.currencyCode,
            salesStatus = product.salesStatus.name,
        )
    }
}
