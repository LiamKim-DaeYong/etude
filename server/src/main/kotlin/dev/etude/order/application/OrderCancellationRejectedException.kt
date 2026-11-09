package dev.etude.order.application

import dev.etude.platform.RejectedException

class OrderCancellationRejectedException(
    override val reason: Reason,
    message: String,
) : RejectedException(message) {

    enum class Reason {
        ORDER_NOT_CANCELABLE,
    }
}
