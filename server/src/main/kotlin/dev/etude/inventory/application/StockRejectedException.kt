package dev.etude.inventory.application

class StockRejectedException(
    val reason: Reason,
    message: String,
) : RuntimeException(message) {

    enum class Reason {
        PRODUCT_NOT_FOUND,
        STOCK_ALREADY_REGISTERED,
    }
}
