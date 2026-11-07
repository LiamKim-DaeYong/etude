package dev.etude.inventory.application

import dev.etude.inventory.domain.Stock
import java.util.UUID

data class StockDetails(
    val productId: UUID,
    val quantity: Int,
) {
    companion object {
        fun from(stock: Stock) = StockDetails(
            productId = stock.productId,
            quantity = stock.quantity,
        )
    }
}
