package com.etude.order.web

import com.etude.order.application.OrderRejectedException.Reason
import com.etude.platform.RejectionStatuses
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class OrderRejectionStatuses : RejectionStatuses<Reason> {

    override val reasonType = Reason::class.java

    override fun statusOf(reason: Reason): HttpStatus = when (reason) {
        Reason.PRODUCT_NOT_FOUND -> HttpStatus.BAD_REQUEST

        Reason.PRODUCT_NOT_ORDERABLE,
        Reason.CURRENCY_MISMATCH,
        Reason.TOTAL_MISMATCH,
        Reason.INSUFFICIENT_INVENTORY,
        -> HttpStatus.CONFLICT
    }
}
