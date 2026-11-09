package com.etude.inventory.application

import com.etude.platform.RejectedException

class InventoryRejectedException(
    override val reason: Reason,
    message: String,
) : RejectedException(message) {

    enum class Reason {
        PRODUCT_NOT_FOUND,
        INVENTORY_ITEM_ALREADY_REGISTERED,
    }
}
