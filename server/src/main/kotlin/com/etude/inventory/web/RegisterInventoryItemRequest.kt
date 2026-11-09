package com.etude.inventory.web

import jakarta.validation.constraints.PositiveOrZero
import java.util.UUID

data class RegisterInventoryItemRequest(
    val productId: UUID,
    @field:PositiveOrZero
    val availableQuantity: Int,
)
