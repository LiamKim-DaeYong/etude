package com.etude.order.web

import com.etude.order.application.OrderCancellationRejectedException.Reason
import com.etude.platform.RejectionStatuses
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class OrderCancellationRejectionStatuses : RejectionStatuses<Reason> {

    override val reasonType = Reason::class.java

    override fun statusOf(reason: Reason): HttpStatus = when (reason) {
        Reason.ORDER_NOT_CANCELABLE -> HttpStatus.CONFLICT
    }
}
