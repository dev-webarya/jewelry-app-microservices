package com.jewelleryapp.controller;

import com.jewelleryapp.dto.request.ReviewRequestDto;
import com.jewelleryapp.dto.response.ReviewResponseDto;
import com.jewelleryapp.service.impl.ReviewServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewServiceImpl reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> addReview(@Valid @RequestBody ReviewRequestDto request) {
        return new ResponseEntity<>(reviewService.addReview(request), HttpStatus.CREATED);
    }

    // Public endpoint to read reviews
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewResponseDto>> getProductReviews(
            @PathVariable UUID productId,
            Pageable pageable) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, pageable));
    }
}