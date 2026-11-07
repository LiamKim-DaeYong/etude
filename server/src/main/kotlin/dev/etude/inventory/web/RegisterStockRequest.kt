package dev.etude.inventory.web

import jakarta.validation.constraints.PositiveOrZero
import java.util.UUID

data class RegisterStockRequest(
    val productId: UUID,
    @field:PositiveOrZero
    val quantity: Int,
)
