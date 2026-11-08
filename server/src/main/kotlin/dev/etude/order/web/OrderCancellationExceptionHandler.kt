package dev.etude.order.web

import dev.etude.order.application.OrderCancellationRejectedException
import dev.etude.order.application.OrderCancellationRejectedException.Reason
import dev.etude.platform.RejectionStatuses
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/** Separate from [OrderExceptionHandler]: one class cannot implement [RejectionStatuses] for two reason types. */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [OrderController::class])
class OrderCancellationExceptionHandler : RejectionStatuses<Reason> {

    override val reasonType = Reason::class.java

    override fun statusOf(reason: Reason): HttpStatus = when (reason) {
        Reason.ORDER_NOT_CANCELABLE -> HttpStatus.CONFLICT
    }

    @ExceptionHandler
    fun handle(e: OrderCancellationRejectedException): ProblemDetail = ProblemDetail.forStatusAndDetail(statusOf(e.reason), e.message).apply {
        setProperty("code", e.reason.name)
    }
}
