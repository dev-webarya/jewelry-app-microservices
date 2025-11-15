package com.jewelleryapp.mapper;

import com.jewelleryapp.dto.request.ProductImageRequestDto;
import com.jewelleryapp.dto.response.ProductImageResponseDto;
import com.jewelleryapp.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductImageMapper {

    @Mapping(target = "productId", source = "product.id")
    // FIX: The property name is 'primary' due to the 'isPrimary()' getter
    @Mapping(target = "primary", source = "primary")
    ProductImageResponseDto toDto(ProductImage productImage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true) // Handled by service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // FIX: The property name is 'primary'
    @Mapping(target = "primary", source = "primary")
    ProductImage toEntity(ProductImageRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true) // Handled by service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // FIX: The property name is 'primary'
    @Mapping(target = "primary", source = "primary")
    void updateEntityFromDto(ProductImageRequestDto requestDto, @MappingTarget ProductImage productImage);
}