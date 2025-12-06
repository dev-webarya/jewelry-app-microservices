package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.StoreRequestDto;
import com.jewelleryapp.dto.response.StoreResponseDto;
import com.jewelleryapp.entity.Store;
import com.jewelleryapp.exception.DuplicateResourceException;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.mapper.StoreMapper;
import com.jewelleryapp.repository.StoreRepository;
import com.jewelleryapp.repository.UserRepository;
import com.jewelleryapp.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository; // Injected for integrity checks
    private final StoreMapper storeMapper;

    @Override
    @Transactional
    public StoreResponseDto createStore(StoreRequestDto requestDto) {
        // Integrity Check: Prevent Duplicate Store Names
        if (storeNameExists(requestDto.getName())) {
            throw new DuplicateResourceException("Store with name '" + requestDto.getName() + "' already exists.");
        }

        Store store = storeMapper.toEntity(requestDto);
        Store savedStore = storeRepository.save(store);
        return storeMapper.toDto(savedStore);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoreResponseDto> getAllStores(Specification<Store> spec, Pageable pageable) {
        return storeRepository.findAll(spec, pageable)
                .map(storeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponseDto getStoreById(UUID id) {
        return storeRepository.findById(id)
                .map(storeMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
    }

    @Override
    @Transactional
    public StoreResponseDto updateStore(UUID id, StoreRequestDto requestDto) {
        Store existingStore = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));

        // Integrity Check: Prevent renaming to an existing store name (excluding self)
        if (!existingStore.getName().equalsIgnoreCase(requestDto.getName()) && storeNameExists(requestDto.getName())) {
            throw new DuplicateResourceException("Store with name '" + requestDto.getName() + "' already exists.");
        }

        storeMapper.updateEntityFromDto(requestDto, existingStore);

        Store updatedStore = storeRepository.save(existingStore);
        return storeMapper.toDto(updatedStore);
    }

    @Override
    @Transactional
    public void deleteStore(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));

        // 1. Business Integrity: Don't delete store if it has active stock
        boolean hasActiveStock = store.getStockItems().stream()
                .anyMatch(item -> item.getQuantity() > 0);

        if (hasActiveStock) {
            throw new DataIntegrityViolationException("Cannot delete store. It still contains active stock items. Please transfer or write off stock first.");
        }

        // 2. Referential Integrity: Don't delete store if Managers are assigned
        // We use the specification executor from UserRepository to check this efficiently
        boolean hasAssignedUsers = userRepository.exists((root, query, cb) ->
                cb.equal(root.get("managedStore").get("id"), id)
        );

        if (hasAssignedUsers) {
            throw new DataIntegrityViolationException("Cannot delete store. There are Store Managers assigned to this location. Reassign them first.");
        }

        // If safe, verify if stock items exist (quantity 0) and allow cascade delete
        // or explicitly warn. Since we passed the active stock check, we assume
        // 0-quantity records are safe to delete (history logs ideally stored elsewhere).
        storeRepository.delete(store);
    }

    // Helper to check for name uniqueness using Specification
    private boolean storeNameExists(String name) {
        return storeRepository.exists((root, query, cb) ->
                cb.equal(cb.lower(root.get("name")), name.toLowerCase())
        );
    }
}