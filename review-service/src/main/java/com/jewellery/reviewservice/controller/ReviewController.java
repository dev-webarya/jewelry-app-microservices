package com.jewellery.reviewservice.controller;

import com.jewellery.common.dto.ApiResponse;
import com.jewellery.common.dto.PageResponse;
import com.jewellery.reviewservice.dto.ReviewRequestDto;
import com.jewellery.reviewservice.dto.ReviewResponseDto;
import com.jewellery.reviewservice.service.ReviewService;
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
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product review management")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Create a review")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(@Valid @RequestBody ReviewRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Review submitted", reviewService.createReview(request)));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get approved reviews for product")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getProductReviews(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getApprovedReviewsByProduct(productId)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews by user")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getUserReviews(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getReviewsByUser(userId)));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending reviews for moderation")
    public ResponseEntity<ApiResponse<List<ReviewResponseDto>>> getPendingReviews() {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getPendingReviews()));
    }

    @GetMapping
    @Operation(summary = "Get all reviews (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponseDto>>> getAllReviews(Pageable pageable) {
        Page<ReviewResponseDto> page = reviewService.getAllReviews(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.<ReviewResponseDto>builder()
                .content(page.getContent()).pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .last(page.isLast()).first(page.isFirst()).build()));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a review")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> approveReview(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Review approved", reviewService.approveReview(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}
