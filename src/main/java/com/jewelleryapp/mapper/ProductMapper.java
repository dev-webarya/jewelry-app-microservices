package com.jewelleryapp.mapper;

import com.jewelleryapp.dto.request.ProductRequestDto;
import com.jewelleryapp.dto.response.ProductResponseDto;
import com.jewelleryapp.entity.AttributeValue;
import com.jewelleryapp.entity.Collection;
import com.jewelleryapp.entity.Product;
import com.jewelleryapp.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CategoryMapper.class} // We can reuse the CategoryMapper
)
public interface ProductMapper {

    // --- Entity to DTO ---

    // This is the main mapping
    @Mapping(target = "category", source = "category") // Uses CategoryMapper
    @Mapping(target = "collections", source = "collections")
    @Mapping(target = "attributes", source = "attributes")
    @Mapping(target = "images", source = "images")
    // FIX: The property name is 'active' due to the 'isActive()' getter
    @Mapping(target = "active", source = "active")
    ProductResponseDto toDto(Product product);

    // Map nested entities to their simple DTOs
    ProductResponseDto.SimpleCollectionDto collectionToSimpleCollectionDto(Collection collection);

    // FIX: The property name is 'primary'
    @Mapping(target = "primary", source = "primary")
    ProductResponseDto.SimpleProductImageDto productImageToSimpleProductImageDto(ProductImage productImage);

    @Mapping(target = "attributeTypeId", source = "attributeType.id")
    @Mapping(target = "attributeTypeName", source = "attributeType.name")
    ProductResponseDto.SimpleAttributeValueDto attributeValueToSimpleAttributeValueDto(AttributeValue attributeValue);


    // --- DTO to Entity ---

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true) // Handled by service
    @Mapping(target = "collections", ignore = true) // Handled by service
    @Mapping(target = "attributes", ignore = true) // Handled by service
    @Mapping(target = "images", ignore = true)
    // FIX: The property name is 'active'
    @Mapping(target = "active", source = "active")
    Product toEntity(ProductRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true) // Handled by service
    @Mapping(target = "collections", ignore = true) // Handled by service
    @Mapping(target = "attributes", ignore = true) // Handled by service
    @Mapping(target = "images", ignore = true)
    // FIX: The property name is 'active'
    @Mapping(target = "active", source = "active")
    void updateEntityFromDto(ProductRequestDto requestDto, @MappingTarget Product product);
}