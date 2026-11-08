package dev.etude.order.application

import dev.etude.shared.Money

class OrderRejectedException(
    val reason: Reason,
    message: String,
    val currentTotal: Money? = null,
) : RuntimeException(message) {

    enum class Reason {
        PRODUCT_NOT_FOUND,
        PRODUCT_NOT_ORDERABLE,
        CURRENCY_MISMATCH,
        TOTAL_MISMATCH,
    }
}
