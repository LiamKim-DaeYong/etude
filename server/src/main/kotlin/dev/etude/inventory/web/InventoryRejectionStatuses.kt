package dev.etude.inventory.web

import dev.etude.inventory.application.InventoryRejectedException.Reason
import dev.etude.platform.RejectionStatuses
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class InventoryRejectionStatuses : RejectionStatuses<Reason> {

    override val reasonType = Reason::class.java

    override fun statusOf(reason: Reason): HttpStatus = when (reason) {
        Reason.PRODUCT_NOT_FOUND -> HttpStatus.BAD_REQUEST
        Reason.INVENTORY_ITEM_ALREADY_REGISTERED -> HttpStatus.CONFLICT
    }
}
