package dev.etude.order.web

import dev.etude.order.application.OrderRejectedException
import dev.etude.order.application.OrderRejectedException.Reason
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [OrderController::class])
class OrderExceptionHandler {

    @ExceptionHandler
    fun handle(e: OrderRejectedException): ProblemDetail {
        val status = when (e.reason) {
            Reason.PRODUCT_NOT_FOUND -> HttpStatus.BAD_REQUEST

            Reason.PRODUCT_NOT_ORDERABLE,
            Reason.CURRENCY_MISMATCH,
            Reason.TOTAL_AMOUNT_MISMATCH,
            -> HttpStatus.CONFLICT
        }
        return ProblemDetail.forStatusAndDetail(status, e.message).apply {
            setProperty("code", e.reason.name)
            e.currentTotal?.let { setProperty("currentTotalAmount", it.amount) }
        }
    }
}
