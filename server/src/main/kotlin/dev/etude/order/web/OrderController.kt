package dev.etude.order.web

import dev.etude.order.application.OrderCancellationRejectedException
import dev.etude.order.application.OrderRejectedException
import dev.etude.order.application.OrderService
import dev.etude.platform.Rejects
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
@Tag(name = "Orders")
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place an order")
    @Rejects(OrderRejectedException.Reason::class)
    fun placeOrder(@Valid @RequestBody request: PlaceOrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.place(request.toCommand())
        return ResponseEntity
            .created(URI.create("/api/v1/orders/${order.id}"))
            .body(OrderResponse.from(order))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order")
    fun getOrder(@PathVariable id: UUID): OrderResponse = orderService.findOrder(id)
        ?.let(OrderResponse::from)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: $id")

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order", description = "Only a placed order can be canceled.")
    @Rejects(OrderCancellationRejectedException.Reason::class)
    fun cancelOrder(@PathVariable id: UUID): OrderResponse = orderService.cancel(id)
        ?.let(OrderResponse::from)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: $id")
}
