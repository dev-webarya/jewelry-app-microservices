package com.jewellery.inventoryservice.service;

import com.jewellery.common.exception.InvalidRequestException;
import com.jewellery.common.exception.ResourceNotFoundException;
import com.jewellery.inventoryservice.dto.StockItemRequestDto;
import com.jewellery.inventoryservice.dto.StockItemResponseDto;
import com.jewellery.inventoryservice.dto.StockReservationRequest;
import com.jewellery.inventoryservice.entity.StockItem;
import com.jewellery.inventoryservice.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockItemService {

    private final StockItemRepository stockItemRepository;

    @Transactional
    public StockItemResponseDto createStockItem(StockItemRequestDto request) {
        // Check if stock already exists for this product/store combination
        if (request.getStoreId() != null) {
            if (stockItemRepository.findByProductIdAndStoreId(request.getProductId(), request.getStoreId())
                    .isPresent()) {
                throw new InvalidRequestException("Stock item already exists for this product and store");
            }
        } else {
            if (stockItemRepository.findByProductIdAndStoreIdIsNull(request.getProductId()).isPresent()) {
                throw new InvalidRequestException("Stock item already exists for this product in central warehouse");
            }
        }

        StockItem stockItem = StockItem.builder()
                .productId(request.getProductId())
                .storeId(request.getStoreId())
                .quantity(request.getQuantity())
                .build();

        return mapToDto(stockItemRepository.save(stockItem));
    }

    @Transactional(readOnly = true)
    public StockItemResponseDto getStockItemById(UUID id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockItem", "id", id));
        return mapToDto(stockItem);
    }

    @Transactional(readOnly = true)
    public StockItemResponseDto getStockByProductId(UUID productId) {
        StockItem stockItem = stockItemRepository.findByProductIdAndStoreIdIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for product in central warehouse"));
        return mapToDto(stockItem);
    }

    @Transactional(readOnly = true)
    public StockItemResponseDto getStockByProductAndStore(UUID productId, UUID storeId) {
        StockItem stockItem = stockItemRepository.findByProductIdAndStoreId(productId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for product in specified store"));
        return mapToDto(stockItem);
    }

    @Transactional(readOnly = true)
    public List<StockItemResponseDto> getStockByStore(UUID storeId) {
        return stockItemRepository.findByStoreId(storeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<StockItemResponseDto> getAllStockItems(Pageable pageable) {
        return stockItemRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional
    public StockItemResponseDto updateQuantity(UUID id, Integer quantity) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockItem", "id", id));
        stockItem.setQuantity(quantity);
        return mapToDto(stockItemRepository.save(stockItem));
    }

    @Transactional
    public boolean reserveStock(StockReservationRequest request) {
        try {
            StockItem stockItem;
            if (request.getStoreId() != null) {
                stockItem = stockItemRepository.findByProductIdAndStoreId(request.getProductId(), request.getStoreId())
                        .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));
            } else {
                stockItem = stockItemRepository.findByProductIdAndStoreIdIsNull(request.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Stock not found in warehouse"));
            }

            if (stockItem.getQuantity() < request.getQuantity()) {
                throw new InvalidRequestException("Insufficient stock. Available: " + stockItem.getQuantity());
            }

            stockItem.setQuantity(stockItem.getQuantity() - request.getQuantity());
            stockItemRepository.save(stockItem);
            log.info("Reserved {} units of product {}", request.getQuantity(), request.getProductId());
            return true;
        } catch (OptimisticLockingFailureException e) {
            throw new InvalidRequestException("Stock was modified by another transaction. Please retry.");
        }
    }

    @Transactional
    public boolean releaseStock(StockReservationRequest request) {
        StockItem stockItem;
        if (request.getStoreId() != null) {
            stockItem = stockItemRepository.findByProductIdAndStoreId(request.getProductId(), request.getStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));
        } else {
            stockItem = stockItemRepository.findByProductIdAndStoreIdIsNull(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stock not found in warehouse"));
        }

        stockItem.setQuantity(stockItem.getQuantity() + request.getQuantity());
        stockItemRepository.save(stockItem);
        log.info("Released {} units of product {}", request.getQuantity(), request.getProductId());
        return true;
    }

    @Transactional
    public void deleteStockItem(UUID id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockItem", "id", id));
        stockItemRepository.delete(stockItem);
    }

    private StockItemResponseDto mapToDto(StockItem stockItem) {
        return StockItemResponseDto.builder()
                .id(stockItem.getId())
                .productId(stockItem.getProductId())
                .storeId(stockItem.getStoreId())
                .quantity(stockItem.getQuantity())
                .createdAt(stockItem.getCreatedAt())
                .updatedAt(stockItem.getUpdatedAt())
                .build();
    }
}
