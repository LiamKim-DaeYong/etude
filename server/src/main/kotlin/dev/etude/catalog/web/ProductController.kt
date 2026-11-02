package dev.etude.catalog.web

import dev.etude.catalog.application.ProductService
import dev.etude.shared.Money
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.Currency
import java.util.UUID

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
) {

    @PostMapping
    fun register(@Valid @RequestBody request: RegisterProductRequest): ResponseEntity<ProductResponse> {
        val price = Money.of(request.priceAmount, Currency.getInstance(request.priceCurrency))
        val product = productService.register(request.name, price)
        return ResponseEntity
            .created(URI.create("/api/products/${product.id}"))
            .body(ProductResponse.from(product))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ResponseEntity<ProductResponse> = productService.getProduct(id)
        ?.let { ResponseEntity.ok(ProductResponse.from(it)) }
        ?: ResponseEntity.notFound().build()
}
