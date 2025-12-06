package com.jewelleryapp.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewRequestDto {
    @NotNull
    private UUID productId;

    @NotNull
    @Min(1) @Max(5)
    private Integer rating;

    @NotBlank
    private String title;

    @NotBlank
    private String comment;
}