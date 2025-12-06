package com.jewelleryapp.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewResponseDto {
    private UUID id;
    private String userName; // "Alice C."
    private Integer rating;
    private String title;
    private String comment;
    private boolean isVerifiedPurchase;
    private LocalDateTime createdAt;
}