package com.jewellery.reviewservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private UUID id;
    private UUID productId;
    private UUID userId;
    private Integer rating;
    private String comment;
    private boolean isApproved;
    private LocalDateTime createdAt;
}
