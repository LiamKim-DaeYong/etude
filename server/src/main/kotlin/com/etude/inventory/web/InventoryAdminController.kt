package com.etude.inventory.web

import com.etude.inventory.application.InventoryRejectedException
import com.etude.inventory.application.InventoryService
import com.etude.platform.Rejects
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.UUID

@RestController
@Tag(name = "Inventory")
@RequestMapping("/admin/v1/inventory-items")
class InventoryAdminController(
    private val inventoryService: InventoryService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register the inventory of a product")
    @Rejects(InventoryRejectedException.Reason::class)
    fun registerInventoryItem(@Valid @RequestBody request: RegisterInventoryItemRequest): ResponseEntity<InventoryItemResponse> {
        val inventoryItem = inventoryService.register(request.productId, request.availableQuantity)
        return ResponseEntity
            .created(URI.create("/admin/v1/inventory-items/${inventoryItem.productId}"))
            .body(InventoryItemResponse.from(inventoryItem))
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get the inventory of a product")
    fun getInventoryItem(@PathVariable productId: UUID): InventoryItemResponse = inventoryService.findInventoryItem(productId)
        ?.let(InventoryItemResponse::from)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found: $productId")
}
