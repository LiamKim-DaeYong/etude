package dev.etude.inventory.domain

import java.util.UUID

class InsufficientInventoryException(
    val productId: UUID,
    val requested: Int,
    val available: Int,
) : RuntimeException("Insufficient inventory for product $productId: requested $requested, available $available")
