package dev.etude.inventory.web

import dev.etude.inventory.application.StockRejectedException
import dev.etude.inventory.application.StockRejectedException.Reason
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [StockAdminController::class])
class StockExceptionHandler {

    @ExceptionHandler
    fun handle(e: StockRejectedException): ProblemDetail {
        val status = when (e.reason) {
            Reason.PRODUCT_NOT_FOUND -> HttpStatus.BAD_REQUEST
            Reason.STOCK_ALREADY_REGISTERED -> HttpStatus.CONFLICT
        }
        return ProblemDetail.forStatusAndDetail(status, e.message).apply {
            setProperty("code", e.reason.name)
        }
    }
}
