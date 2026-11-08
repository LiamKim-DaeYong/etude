package dev.etude.inventory.application

class InventoryRejectedException(
    val reason: Reason,
    message: String,
) : RuntimeException(message) {

    enum class Reason {
        PRODUCT_NOT_FOUND,
        INVENTORY_ITEM_ALREADY_REGISTERED,
    }
}
