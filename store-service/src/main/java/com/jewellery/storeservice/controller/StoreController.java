package com.jewellery.storeservice.controller;

import com.jewellery.common.dto.ApiResponse;
import com.jewellery.common.dto.PageResponse;
import com.jewellery.storeservice.dto.StoreRequestDto;
import com.jewellery.storeservice.dto.StoreResponseDto;
import com.jewellery.storeservice.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Tag(name = "Stores", description = "Store management")
public class StoreController {
    private final StoreService storeService;

    @PostMapping
    @Operation(summary = "Create a new store")
    public ResponseEntity<ApiResponse<StoreResponseDto>> createStore(@Valid @RequestBody StoreRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Store created", storeService.createStore(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get store by ID")
    public ResponseEntity<ApiResponse<StoreResponseDto>> getStoreById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(storeService.getStoreById(id)));
    }

    @GetMapping
    @Operation(summary = "Get all stores (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<StoreResponseDto>>> getAllStores(Pageable pageable) {
        Page<StoreResponseDto> page = storeService.getAllStores(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.<StoreResponseDto>builder()
                .content(page.getContent()).pageNumber(page.getNumber()).pageSize(page.getSize())
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .last(page.isLast()).first(page.isFirst()).build()));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active stores")
    public ResponseEntity<ApiResponse<List<StoreResponseDto>>> getActiveStores() {
        return ResponseEntity.ok(ApiResponse.success(storeService.getActiveStores()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update store")
    public ResponseEntity<ApiResponse<StoreResponseDto>> updateStore(@PathVariable UUID id,
            @Valid @RequestBody StoreRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Store updated", storeService.updateStore(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete store")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable UUID id) {
        storeService.deleteStore(id);
        return ResponseEntity.ok(ApiResponse.success("Store deleted", null));
    }
}
