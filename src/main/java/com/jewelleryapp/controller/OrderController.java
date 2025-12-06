package com.jewelleryapp.controller;

import com.jewelleryapp.dto.request.CartCheckoutRequestDto;
import com.jewelleryapp.dto.request.OrderRequestDto;
import com.jewelleryapp.dto.response.OrderResponseDto;
import com.jewelleryapp.enums.OrderStatus;
import com.jewelleryapp.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 1. Buy Now (Specific items provided in request)
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto request) {
        return new ResponseEntity<>(orderService.createOrder(request), HttpStatus.CREATED);
    }

    // 2. Checkout Cart (Buy everything currently in the user's cart)
    @PostMapping("/checkout-cart")
    public ResponseEntity<OrderResponseDto> checkoutCart(@Valid @RequestBody CartCheckoutRequestDto request) {
        return new ResponseEntity<>(orderService.checkoutCart(request), HttpStatus.CREATED);
    }

    // 3. Get All Orders (Dynamic Search & Filter)
    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) UUID userId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OrderResponseDto> orders = orderService.getAllOrders(
                status, orderNumber, minPrice, maxPrice, startDate, endDate, userId, pageable
        );
        return ResponseEntity.ok(orders);
    }

    // 4. Get Order By ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // 5. Update Status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STORE_MANAGER')")
    public ResponseEntity<OrderResponseDto> updateStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status, notes));
    }
}