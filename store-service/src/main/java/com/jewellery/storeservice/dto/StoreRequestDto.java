package com.jewellery.storeservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StoreRequestDto {
    @NotBlank(message = "Store name is required")
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String operatingHours;
    private String contactPhone;
    private String email;
    private boolean isActive = true;
}
