package dev.etude.order

import java.util.UUID

/** Published in the transaction that cancels an order. */
data class OrderCanceled(val orderId: UUID)
