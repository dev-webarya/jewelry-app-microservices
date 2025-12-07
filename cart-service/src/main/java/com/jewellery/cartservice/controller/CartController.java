package com.jewellery.cartservice.controller;

import com.jewellery.cartservice.dto.AddToCartRequest;
import com.jewellery.cartservice.dto.CartResponseDto;
import com.jewellery.cartservice.service.CartService;
import com.jewellery.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Cart management")
public class CartController {
    private final CartService cartService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get cart by user ID")
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCartByUserId(userId)));
    }

    @PostMapping("/{userId}/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponseDto>> addToCart(
            @PathVariable UUID userId,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cartService.addToCart(userId, request)));
    }

    @PutMapping("/{userId}/items/{productId}")
    @Operation(summary = "Update item quantity")
    public ResponseEntity<ApiResponse<CartResponseDto>> updateQuantity(
            @PathVariable UUID userId,
            @PathVariable UUID productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(
                ApiResponse.success("Quantity updated", cartService.updateItemQuantity(userId, productId, quantity)));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponseDto>> removeFromCart(
            @PathVariable UUID userId,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success("Item removed", cartService.removeFromCart(userId, productId)));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
