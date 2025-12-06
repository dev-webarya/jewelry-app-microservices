package com.jewelleryapp.controller;

import com.jewelleryapp.dto.request.StoreRequestDto;
import com.jewelleryapp.dto.response.StoreResponseDto;
import com.jewelleryapp.entity.Store;
import com.jewelleryapp.service.StoreService;
import com.jewelleryapp.specification.StoreSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    @Autowired
    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreResponseDto> createStore(@Valid @RequestBody StoreRequestDto requestDto) {
        StoreResponseDto createdDto = storeService.createStore(requestDto);
        return new ResponseEntity<>(createdDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponseDto> getStoreById(@PathVariable UUID id) {
        StoreResponseDto dto = storeService.getStoreById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<Page<StoreResponseDto>> getAllStores(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            Pageable pageable) {

        Specification<Store> spec = Specification.where(StoreSpecification.hasName(name))
                .and(StoreSpecification.hasAddress(address));

        Page<StoreResponseDto> dtos = storeService.getAllStores(spec, pageable);
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreResponseDto> updateStore(@PathVariable UUID id, @Valid @RequestBody StoreRequestDto requestDto) {
        StoreResponseDto updatedDto = storeService.updateStore(id, requestDto);
        return ResponseEntity.ok(updatedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable UUID id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}