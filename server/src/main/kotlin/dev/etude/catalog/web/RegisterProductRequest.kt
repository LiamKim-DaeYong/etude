package dev.etude.catalog.web

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class RegisterProductRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val name: String,
    @field:DecimalMin("0")
    val priceAmount: BigDecimal,
    @field:Size(min = 3, max = 3)
    val priceCurrency: String,
)
