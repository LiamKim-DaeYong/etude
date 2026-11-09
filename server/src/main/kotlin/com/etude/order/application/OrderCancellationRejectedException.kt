package com.etude.order.application

import com.etude.platform.RejectedException

class OrderCancellationRejectedException(
    override val reason: Reason,
    message: String,
) : RejectedException(message) {

    enum class Reason {
        ORDER_NOT_CANCELABLE,
    }
}
