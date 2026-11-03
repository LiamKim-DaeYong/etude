package dev.etude.catalog.web

import dev.etude.catalog.application.ProductService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
) {

    @PostMapping
    fun register(@Valid @RequestBody request: RegisterProductRequest): ResponseEntity<ProductResponse> {
        val product = productService.register(request.name, request.toMoney())
        return ResponseEntity
            .created(URI.create("/api/products/${product.id}"))
            .body(ProductResponse.from(product))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ProductResponse = productService.getProduct(id)
        ?.let(ProductResponse::from)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: $id")
}
