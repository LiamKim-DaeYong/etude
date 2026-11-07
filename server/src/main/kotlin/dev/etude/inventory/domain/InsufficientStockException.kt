package dev.etude.inventory.domain

import java.util.UUID

class InsufficientStockException(
    val productId: UUID,
    val requested: Int,
    val available: Int,
) : RuntimeException("Insufficient stock for product $productId: requested $requested, available $available")
