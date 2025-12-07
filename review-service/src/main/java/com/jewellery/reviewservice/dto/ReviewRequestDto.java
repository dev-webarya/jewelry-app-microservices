package com.jewellery.reviewservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.UUID;

@Data
public class ReviewRequestDto {
    @NotNull
    private UUID productId;
    @NotNull
    private UUID userId;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    private String comment;
}
