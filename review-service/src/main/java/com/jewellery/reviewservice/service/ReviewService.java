package com.jewellery.reviewservice.service;

import com.jewellery.common.exception.ResourceNotFoundException;
import com.jewellery.reviewservice.dto.ReviewRequestDto;
import com.jewellery.reviewservice.dto.ReviewResponseDto;
import com.jewellery.reviewservice.entity.Review;
import com.jewellery.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto request) {
        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(request.getUserId())
                .rating(request.getRating())
                .comment(request.getComment())
                .isApproved(false)
                .build();
        return mapToDto(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getApprovedReviewsByProduct(UUID productId) {
        return reviewRepository.findByProductIdAndIsApprovedTrue(productId).stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewsByUser(UUID userId) {
        return reviewRepository.findByUserId(userId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getPendingReviews() {
        return reviewRepository.findByIsApprovedFalse().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional
    public ReviewResponseDto approveReview(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        review.setApproved(true);
        return mapToDto(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        reviewRepository.delete(review);
    }

    private ReviewResponseDto mapToDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId()).productId(review.getProductId()).userId(review.getUserId())
                .rating(review.getRating()).comment(review.getComment()).isApproved(review.isApproved())
                .createdAt(review.getCreatedAt()).build();
    }
}
