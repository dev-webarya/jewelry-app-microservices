package com.jewelleryapp.repository;

import com.jewelleryapp.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByProductId(UUID productId, Pageable pageable);

    // Check if user already reviewed this product
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
}