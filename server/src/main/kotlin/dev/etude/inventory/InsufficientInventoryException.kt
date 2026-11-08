package dev.etude.inventory

import java.util.UUID

class InsufficientInventoryException(
    val productId: UUID,
    val requestedQuantity: Int,
    val availableQuantity: Int,
) : RuntimeException(
    "Insufficient inventory for product $productId: requested $requestedQuantity, available $availableQuantity",
)
