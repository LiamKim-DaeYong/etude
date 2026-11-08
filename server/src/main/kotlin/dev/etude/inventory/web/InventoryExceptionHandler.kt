package dev.etude.inventory.web

import dev.etude.inventory.application.InventoryRejectedException
import dev.etude.inventory.application.InventoryRejectedException.Reason
import dev.etude.platform.RejectionStatuses
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = [InventoryAdminController::class])
class InventoryExceptionHandler : RejectionStatuses<Reason> {

    override val reasonType = Reason::class.java

    override fun statusOf(reason: Reason): HttpStatus = when (reason) {
        Reason.PRODUCT_NOT_FOUND -> HttpStatus.BAD_REQUEST
        Reason.INVENTORY_ITEM_ALREADY_REGISTERED -> HttpStatus.CONFLICT
    }

    @ExceptionHandler
    fun handle(e: InventoryRejectedException): ProblemDetail = ProblemDetail.forStatusAndDetail(statusOf(e.reason), e.message).apply {
        setProperty("code", e.reason.name)
    }
}
