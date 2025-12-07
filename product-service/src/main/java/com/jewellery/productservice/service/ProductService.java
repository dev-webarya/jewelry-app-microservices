package com.jewellery.productservice.service;

import com.jewellery.common.exception.DuplicateResourceException;
import com.jewellery.common.exception.ResourceNotFoundException;
import com.jewellery.productservice.dto.ProductRequestDto;
import com.jewellery.productservice.dto.ProductResponseDto;
import com.jewellery.productservice.entity.Category;
import com.jewellery.productservice.entity.Collection;
import com.jewellery.productservice.entity.Product;
import com.jewellery.productservice.repository.CategoryRepository;
import com.jewellery.productservice.repository.CollectionRepository;
import com.jewellery.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .isActive(request.isActive())
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getCollectionIds() != null && !request.getCollectionIds().isEmpty()) {
            Set<Collection> collections = new HashSet<>();
            for (UUID collectionId : request.getCollectionIds()) {
                Collection collection = collectionRepository.findById(collectionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", collectionId));
                collections.add(collection);
            }
            product.setCollections(collections);
        }

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDto(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional
    public ProductResponseDto updateProduct(UUID id, ProductRequestDto request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setActive(request.isActive());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
    }

    private ProductResponseDto mapToDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .isActive(product.isActive())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .collections(product.getCollections().stream().map(Collection::getName).collect(Collectors.toList()))
                .images(product.getImages().stream()
                        .map(img -> ProductResponseDto.ProductImageDto.builder()
                                .id(img.getId())
                                .imageUrl(img.getImageUrl())
                                .isPrimary(img.isPrimary())
                                .displayOrder(img.getDisplayOrder())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
