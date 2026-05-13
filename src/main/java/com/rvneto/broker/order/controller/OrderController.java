package com.rvneto.broker.order.controller;

import com.rvneto.broker.order.domain.Order;
import com.rvneto.broker.order.dto.OrderRequestDTO;
import com.rvneto.broker.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints for placing and querying market orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place a new order",
               description = "Submits a new BUY or SELL order. userId is extracted from the JWT token via gateway header X-User-Id.")
    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody OrderRequestDTO request) {
        request.setUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(request));
    }

    @Operation(summary = "Get order by ID",
               description = "Returns the details of a specific order")
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "List orders by user",
               description = "Returns all orders placed by a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        return ResponseEntity.ok(orderService.findByUserId(userId));
    }
}
