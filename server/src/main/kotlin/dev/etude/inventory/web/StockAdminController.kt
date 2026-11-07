package dev.etude.inventory.web

import dev.etude.inventory.application.StockService
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
@RequestMapping("/admin/v1/stocks")
class StockAdminController(
    private val stockService: StockService,
) {

    @PostMapping
    fun register(@Valid @RequestBody request: RegisterStockRequest): ResponseEntity<StockResponse> {
        val stock = stockService.register(request.productId, request.quantity)
        return ResponseEntity
            .created(URI.create("/admin/v1/stocks/${stock.productId}"))
            .body(StockResponse.from(stock))
    }

    @GetMapping("/{productId}")
    fun get(@PathVariable productId: UUID): StockResponse = stockService.getStock(productId)
        ?.let(StockResponse::from)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found: $productId")
}
