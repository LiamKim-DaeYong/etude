package com.etude.catalog.web

import com.etude.catalog.application.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@Tag(name = "Products")
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {

    @GetMapping("/{id}")
    @Operation(summary = "Get a product")
    fun getProduct(@PathVariable id: UUID): ProductResponse = productService.findProduct(id)
        ?.let(ProductResponse::from)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: $id")
}
