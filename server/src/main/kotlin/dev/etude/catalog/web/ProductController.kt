package dev.etude.catalog.web

import dev.etude.catalog.application.ProductService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: UUID): ProductResponse = productService.getProduct(id)
        ?.let(ProductResponse::from)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: $id")
}
