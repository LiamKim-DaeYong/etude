package dev.etude.catalog.web

import dev.etude.shared.Money
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterProductRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val name: String,
    val price: Money,
)
