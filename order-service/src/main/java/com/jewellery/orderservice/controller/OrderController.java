package com.jewellery.orderservice.controller;

import com.jewellery.common.dto.ApiResponse;
import com.jewellery.common.dto.PageResponse;
import com.jewellery.orderservice.dto.CreateOrderRequest;
import com.jewellery.orderservice.dto.OrderResponseDto;
import com.jewellery.orderservice.enums.OrderStatus;
import com.jewellery.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order created", orderService.createOrder(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id)));
    }

    @GetMapping("/by-number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderByNumber(orderNumber)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user ID")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getOrdersByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByUserId(userId)));
    }

    @GetMapping
    @Operation(summary = "Get all orders (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponseDto>>> getAllOrders(Pageable pageable) {
        Page<OrderResponseDto> page = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.<OrderResponseDto>builder()
                .content(page.getContent()).pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .last(page.isLast()).first(page.isFirst()).build()));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateStatus(@PathVariable UUID id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", orderService.updateOrderStatus(id, status)));
    }

    @PostMapping("/{id}/confirm-payment")
    @Operation(summary = "Confirm order payment")
    public ResponseEntity<ApiResponse<OrderResponseDto>> confirmPayment(
            @PathVariable UUID id,
            @RequestParam String paymentMethod,
            @RequestParam String transactionId) {
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed",
                orderService.confirmPayment(id, paymentMethod, transactionId)));
    }
}
