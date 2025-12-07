package com.jewellery.storeservice.service;

import com.jewellery.common.exception.ResourceNotFoundException;
import com.jewellery.storeservice.dto.StoreRequestDto;
import com.jewellery.storeservice.dto.StoreResponseDto;
import com.jewellery.storeservice.entity.Store;
import com.jewellery.storeservice.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponseDto createStore(StoreRequestDto request) {
        Store store = Store.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .operatingHours(request.getOperatingHours())
                .contactPhone(request.getContactPhone())
                .email(request.getEmail())
                .isActive(request.isActive())
                .build();
        return mapToDto(storeRepository.save(store));
    }

    @Transactional(readOnly = true)
    public StoreResponseDto getStoreById(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
        return mapToDto(store);
    }

    @Transactional(readOnly = true)
    public Page<StoreResponseDto> getAllStores(Pageable pageable) {
        return storeRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<StoreResponseDto> getActiveStores() {
        return storeRepository.findByIsActiveTrue().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public StoreResponseDto updateStore(UUID id, StoreRequestDto request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        store.setOperatingHours(request.getOperatingHours());
        store.setContactPhone(request.getContactPhone());
        store.setEmail(request.getEmail());
        store.setActive(request.isActive());
        return mapToDto(storeRepository.save(store));
    }

    @Transactional
    public void deleteStore(UUID id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
        storeRepository.delete(store);
    }

    private StoreResponseDto mapToDto(Store store) {
        return StoreResponseDto.builder()
                .id(store.getId()).name(store.getName()).address(store.getAddress())
                .latitude(store.getLatitude()).longitude(store.getLongitude())
                .operatingHours(store.getOperatingHours()).contactPhone(store.getContactPhone())
                .email(store.getEmail()).isActive(store.isActive())
                .createdAt(store.getCreatedAt()).updatedAt(store.getUpdatedAt())
                .build();
    }
}
