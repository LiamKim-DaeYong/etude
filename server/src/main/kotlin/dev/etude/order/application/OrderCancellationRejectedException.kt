package dev.etude.order.application

class OrderCancellationRejectedException(
    val reason: Reason,
    message: String,
) : RuntimeException(message) {

    enum class Reason {
        ORDER_NOT_CANCELABLE,
    }
}
