package dev.etude.order.web

import dev.etude.order.application.OrderRejectedException
import dev.etude.order.application.OrderRejectedException.Reason
import dev.etude.platform.RejectionStatuses
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [OrderController::class])
class OrderExceptionHandler : RejectionStatuses<Reason> {

    override val reasonType = Reason::class.java

    override fun statusOf(reason: Reason): HttpStatus = when (reason) {
        Reason.PRODUCT_NOT_FOUND -> HttpStatus.BAD_REQUEST

        Reason.PRODUCT_NOT_ORDERABLE,
        Reason.CURRENCY_MISMATCH,
        Reason.TOTAL_AMOUNT_MISMATCH,
        -> HttpStatus.CONFLICT
    }

    @ExceptionHandler
    fun handle(e: OrderRejectedException): ProblemDetail = ProblemDetail.forStatusAndDetail(statusOf(e.reason), e.message).apply {
        setProperty("code", e.reason.name)
        e.currentTotal?.let { setProperty("currentTotal", it) }
    }
}
