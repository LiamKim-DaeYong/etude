package dev.etude.catalog.web

import dev.etude.catalog.application.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@Tag(name = "Products")
@RequestMapping("/admin/v1/products")
class ProductAdminController(
    private val productService: ProductService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a product")
    fun registerProduct(@Valid @RequestBody request: RegisterProductRequest): ResponseEntity<ProductResponse> {
        val product = productService.register(request.name, request.price)
        return ResponseEntity
            .created(URI.create("/api/v1/products/${product.id}"))
            .body(ProductResponse.from(product))
    }
}
