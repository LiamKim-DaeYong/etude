package dev.etude.order.web

import dev.etude.order.application.OrderService
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
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun placeOrder(@Valid @RequestBody request: PlaceOrderRequest): ResponseEntity<OrderResponse> {
        val order = orderService.place(request.toCommand())
        return ResponseEntity
            .created(URI.create("/api/v1/orders/${order.id}"))
            .body(OrderResponse.from(order))
    }

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: UUID): OrderResponse = orderService.getOrder(id)
        ?.let(OrderResponse::from)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: $id")
}
