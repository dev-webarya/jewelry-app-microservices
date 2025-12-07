package com.jewellery.reviewservice.repository;

import com.jewellery.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByProductIdAndIsApprovedTrue(UUID productId);

    List<Review> findByUserId(UUID userId);

    List<Review> findByIsApprovedFalse();
}
