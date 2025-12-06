package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.CartItemRequestDto;
import com.jewelleryapp.dto.response.CartResponseDto;
import com.jewelleryapp.entity.CartItem;
import com.jewelleryapp.entity.Product;
import com.jewelleryapp.entity.ShoppingCart;
import com.jewelleryapp.entity.User;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.mapper.EngagementMapper;
import com.jewelleryapp.repository.CartRepository;
import com.jewelleryapp.repository.ProductRepository;
import com.jewelleryapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EngagementMapper engagementMapper;

    @Transactional
    public CartResponseDto addToCart(CartItemRequestDto request) {
        User user = getCurrentUser();
        ShoppingCart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Check if item exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        return engagementMapper.toCartDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponseDto removeFromCart(UUID productId) {
        User user = getCurrentUser();
        ShoppingCart cart = getOrCreateCart(user);

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        return engagementMapper.toCartDto(cartRepository.save(cart));
    }

    @Transactional(readOnly = true)
    public CartResponseDto getMyCart() {
        return engagementMapper.toCartDto(getOrCreateCart(getCurrentUser()));
    }

    @Transactional
    public void clearCart() {
        ShoppingCart cart = getOrCreateCart(getCurrentUser());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private ShoppingCart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(ShoppingCart.builder().user(user).build()));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }
}