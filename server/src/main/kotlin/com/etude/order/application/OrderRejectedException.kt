package com.etude.order.application

import com.etude.platform.RejectedException
import com.etude.shared.Money
import java.util.UUID

class OrderRejectedException(
    override val reason: Reason,
    message: String,
    val currentTotal: Money? = null,
    val productId: UUID? = null,
) : RejectedException(message) {

    override val properties: Map<String, Any>
        get() = buildMap {
            currentTotal?.let { put("currentTotal", it) }
            productId?.let { put("productId", it) }
        }

    enum class Reason {
        PRODUCT_NOT_FOUND,
        PRODUCT_NOT_ORDERABLE,
        CURRENCY_MISMATCH,
        TOTAL_MISMATCH,
        INSUFFICIENT_INVENTORY,
    }
}
