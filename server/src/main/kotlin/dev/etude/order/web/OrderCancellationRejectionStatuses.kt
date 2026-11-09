package dev.etude.order.web

import dev.etude.order.application.OrderCancellationRejectedException.Reason
import dev.etude.platform.RejectionStatuses
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class OrderCancellationRejectionStatuses : RejectionStatuses<Reason> {

    override val reasonType = Reason::class.java

    override fun statusOf(reason: Reason): HttpStatus = when (reason) {
        Reason.ORDER_NOT_CANCELABLE -> HttpStatus.CONFLICT
    }
}
