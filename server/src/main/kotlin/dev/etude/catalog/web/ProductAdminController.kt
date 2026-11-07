package dev.etude.catalog.web

import dev.etude.catalog.application.ProductService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/admin/v1/products")
class ProductAdminController(
    private val productService: ProductService,
) {

    @PostMapping
    fun register(@Valid @RequestBody request: RegisterProductRequest): ResponseEntity<ProductResponse> {
        val product = productService.register(request.name, request.toMoney())
        return ResponseEntity
            .created(URI.create("/api/v1/products/${product.id}"))
            .body(ProductResponse.from(product))
    }
}
