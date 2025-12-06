package com.jewelleryapp.controller;

import com.jewelleryapp.dto.request.CartItemRequestDto;
import com.jewelleryapp.dto.response.CartResponseDto;
import com.jewelleryapp.service.impl.CartServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartServiceImpl cartService;

    @GetMapping
    public ResponseEntity<CartResponseDto> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItem(@Valid @RequestBody CartItemRequestDto request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDto> removeItem(@PathVariable UUID productId) {
        return ResponseEntity.ok(cartService.removeFromCart(productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}