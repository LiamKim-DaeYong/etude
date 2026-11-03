package dev.etude.catalog.web

import dev.etude.shared.Money
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.Currency

data class RegisterProductRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val name: String,
    @field:DecimalMin("0")
    val priceAmount: BigDecimal,
    @field:Size(min = 3, max = 3)
    val priceCurrency: String,
) {
    fun toMoney(): Money = try {
        Money.of(priceAmount, Currency.getInstance(priceCurrency))
    } catch (e: IllegalArgumentException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency: $priceCurrency", e)
    }
}
