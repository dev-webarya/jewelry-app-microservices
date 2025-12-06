package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.ProductRequestDto;
import com.jewelleryapp.dto.response.ProductResponseDto;
import com.jewelleryapp.entity.AttributeValue;
import com.jewelleryapp.entity.Category;
import com.jewelleryapp.entity.Collection;
import com.jewelleryapp.entity.Product;
import com.jewelleryapp.exception.DuplicateResourceException;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.mapper.ProductMapper;
import com.jewelleryapp.repository.AttributeValueRepository;
import com.jewelleryapp.repository.CategoryRepository;
import com.jewelleryapp.repository.CollectionRepository;
import com.jewelleryapp.repository.ProductRepository;
import com.jewelleryapp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        if (skuExists(requestDto.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + requestDto.getSku() + "' already exists.");
        }

        Product product = productMapper.toEntity(requestDto);
        updateProductRelationships(product, requestDto);

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Specification<Product> spec, Pageable pageable) {
        return productRepository.findAll(spec, pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(UUID id, ProductRequestDto requestDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!existingProduct.getSku().equalsIgnoreCase(requestDto.getSku()) && skuExists(requestDto.getSku())) {
            throw new DuplicateResourceException("Product with SKU '" + requestDto.getSku() + "' already exists.");
        }

        productMapper.updateEntityFromDto(requestDto, existingProduct);
        updateProductRelationships(existingProduct, requestDto);

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        // Note: Cascade deletes on images and stock items are usually desired behavior for Products.
        // If you want to prevent deleting products with stock, you'd add a check here against StockItemRepository.
        productRepository.delete(product);
    }

    private void updateProductRelationships(Product product, ProductRequestDto dto) {
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        product.getCollections().clear();
        if (dto.getCollectionIds() != null && !dto.getCollectionIds().isEmpty()) {
            Set<Collection> collections = new HashSet<>(collectionRepository.findAllById(dto.getCollectionIds()));
            if (collections.size() != dto.getCollectionIds().size()) {
                throw new ResourceNotFoundException("One or more Collections not found");
            }
            product.setCollections(collections);
        }

        product.getAttributes().clear();
        if (dto.getAttributeValueIds() != null && !dto.getAttributeValueIds().isEmpty()) {
            Set<AttributeValue> attributes = new HashSet<>(attributeValueRepository.findAllById(dto.getAttributeValueIds()));
            if (attributes.size() != dto.getAttributeValueIds().size()) {
                throw new ResourceNotFoundException("One or more AttributeValues not found");
            }
            product.setAttributes(attributes);
        }
    }

    private boolean skuExists(String sku) {
        return productRepository.exists((root, query, cb) ->
                cb.equal(cb.lower(root.get("sku")), sku.toLowerCase())
        );
    }
}