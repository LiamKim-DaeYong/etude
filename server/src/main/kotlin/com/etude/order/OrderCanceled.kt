package com.etude.order

import java.util.UUID

/** 주문을 취소한 트랜잭션에서 발행한다. */
data class OrderCanceled(val orderId: UUID)
