package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.ReviewRequestDto;
import com.jewelleryapp.dto.response.ReviewResponseDto;
import com.jewelleryapp.entity.*;
import com.jewelleryapp.enums.OrderStatus;
import com.jewelleryapp.exception.DuplicateResourceException;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.mapper.EngagementMapper;
import com.jewelleryapp.repository.OrderRepository;
import com.jewelleryapp.repository.ProductRepository;
import com.jewelleryapp.repository.ReviewRepository;
import com.jewelleryapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EngagementMapper engagementMapper;

    @Transactional
    public ReviewResponseDto addReview(ReviewRequestDto request) {
        User user = getCurrentUser();

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), request.getProductId())) {
            throw new DuplicateResourceException("You have already reviewed this product.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // --- AUTOMATIC VERIFICATION LOGIC ---
        // Check if user has a DELIVERED order containing this product
        CustomerOrder verifiedOrder = null;

        // This is a naive fetch all approach. In huge systems, we would use a custom query.
        // But for this scope, fetching user orders is fine.
        List<CustomerOrder> userOrders = orderRepository.findAll((root, query, cb) ->
                cb.equal(root.get("user").get("id"), user.getId())
        );

        for (CustomerOrder order : userOrders) {
            if (order.getStatus() == OrderStatus.DELIVERED) {
                boolean hasProduct = order.getItems().stream()
                        .anyMatch(item -> item.getProduct().getId().equals(product.getId()));
                if (hasProduct) {
                    verifiedOrder = order;
                    break;
                }
            }
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .verifiedOrder(verifiedOrder)
                .isVerifiedPurchase(verifiedOrder != null)
                .build();

        return engagementMapper.toReviewDto(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getProductReviews(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(engagementMapper::toReviewDto);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }
}