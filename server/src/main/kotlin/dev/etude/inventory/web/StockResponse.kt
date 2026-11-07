package dev.etude.inventory.web

import dev.etude.inventory.application.StockDetails
import java.util.UUID

data class StockResponse(
    val productId: UUID,
    val quantity: Int,
) {
    companion object {
        fun from(stock: StockDetails) = StockResponse(
            productId = stock.productId,
            quantity = stock.quantity,
        )
    }
}
