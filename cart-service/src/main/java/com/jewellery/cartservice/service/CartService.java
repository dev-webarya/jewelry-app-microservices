package com.jewellery.cartservice.service;

import com.jewellery.cartservice.client.ProductClient;
import com.jewellery.cartservice.dto.AddToCartRequest;
import com.jewellery.cartservice.dto.CartResponseDto;
import com.jewellery.cartservice.entity.CartItem;
import com.jewellery.cartservice.entity.ShoppingCart;
import com.jewellery.cartservice.repository.CartRepository;
import com.jewellery.common.exception.InvalidRequestException;
import com.jewellery.common.exception.ResourceNotFoundException;
import com.jewellery.common.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    private final CartRepository cartRepository;
    private final ProductClient productClient;

    @Transactional(readOnly = true)
    public CartResponseDto getCartByUserId(UUID userId) {
        ShoppingCart cart = getOrCreateCart(userId);
        return mapToDto(cart);
    }

    @Transactional
    public CartResponseDto addToCart(UUID userId, AddToCartRequest request) {
        ShoppingCart cart = getOrCreateCart(userId);

        // Validate product exists via Feign (optional - graceful degradation)
        ProductClient.ProductDto product = null;
        try {
            var response = productClient.getProductById(request.getProductId());
            if (response != null && response.getData() != null) {
                product = response.getData();
                if (!product.isActive()) {
                    throw new InvalidRequestException("Product is not active");
                }
            }
        } catch (Exception e) {
            log.warn("Could not validate product {}: {}", request.getProductId(), e.getMessage());
            // Allow adding to cart even if product service is down - validate at checkout
        }

        // Check if item already in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        return mapToDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDto updateItemQuantity(UUID userId, UUID productId, Integer quantity) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        return mapToDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDto removeFromCart(UUID userId, UUID productId) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return mapToDto(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(UUID userId) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private ShoppingCart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(ShoppingCart.builder().userId(userId).build()));
    }

    private CartResponseDto mapToDto(ShoppingCart cart) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        var items = cart.getItems().stream().map(item -> {
            String productName = "Product " + item.getProductId().toString().substring(0, 8);
            BigDecimal productPrice = BigDecimal.ZERO;

            try {
                var response = productClient.getProductById(item.getProductId());
                if (response != null && response.getData() != null) {
                    productName = response.getData().name();
                    productPrice = response.getData().basePrice();
                }
            } catch (Exception e) {
                log.warn("Could not fetch product details for {}", item.getProductId());
            }

            BigDecimal subtotal = productPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            return CartResponseDto.CartItemDto.builder()
                    .id(item.getId())
                    .productId(item.getProductId())
                    .productName(productName)
                    .productPrice(productPrice)
                    .quantity(item.getQuantity())
                    .subtotal(subtotal)
                    .build();
        }).collect(Collectors.toList());

        totalPrice = items.stream()
                .map(CartResponseDto.CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponseDto.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(items)
                .totalPrice(totalPrice)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
