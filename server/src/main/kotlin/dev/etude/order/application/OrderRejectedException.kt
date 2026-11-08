package dev.etude.order.application

import dev.etude.shared.Money
import java.util.UUID

class OrderRejectedException(
    val reason: Reason,
    message: String,
    val currentTotal: Money? = null,
    val productId: UUID? = null,
) : RuntimeException(message) {

    enum class Reason {
        PRODUCT_NOT_FOUND,
        PRODUCT_NOT_ORDERABLE,
        CURRENCY_MISMATCH,
        TOTAL_MISMATCH,
        INSUFFICIENT_INVENTORY,
    }
}
