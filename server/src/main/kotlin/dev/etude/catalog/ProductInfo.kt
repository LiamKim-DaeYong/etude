package dev.etude.catalog

import dev.etude.shared.Money
import java.util.UUID

data class ProductInfo(
    val id: UUID,
    val name: String,
    val price: Money,
    val orderable: Boolean,
)
