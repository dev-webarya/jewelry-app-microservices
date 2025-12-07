package com.jewellery.inventoryservice.controller;

import com.jewellery.common.dto.ApiResponse;
import com.jewellery.common.dto.PageResponse;
import com.jewellery.inventoryservice.dto.StockItemRequestDto;
import com.jewellery.inventoryservice.dto.StockItemResponseDto;
import com.jewellery.inventoryservice.dto.StockReservationRequest;
import com.jewellery.inventoryservice.service.StockItemService;
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
@RequestMapping("/api/v1/stock-items")
@RequiredArgsConstructor
@Tag(name = "Stock Items", description = "Inventory and stock management")
public class StockItemController {

    private final StockItemService stockItemService;

    @PostMapping
    @Operation(summary = "Create a new stock item")
    public ResponseEntity<ApiResponse<StockItemResponseDto>> createStockItem(
            @Valid @RequestBody StockItemRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success("Stock item created", stockItemService.createStockItem(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get stock item by ID")
    public ResponseEntity<ApiResponse<StockItemResponseDto>> getStockItemById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(stockItemService.getStockItemById(id)));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stock for product in central warehouse")
    public ResponseEntity<ApiResponse<StockItemResponseDto>> getStockByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(stockItemService.getStockByProductId(productId)));
    }

    @GetMapping("/product/{productId}/store/{storeId}")
    @Operation(summary = "Get stock for product in specific store")
    public ResponseEntity<ApiResponse<StockItemResponseDto>> getStockByProductAndStore(
            @PathVariable UUID productId,
            @PathVariable UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success(stockItemService.getStockByProductAndStore(productId, storeId)));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "Get all stock items for a store")
    public ResponseEntity<ApiResponse<List<StockItemResponseDto>>> getStockByStore(@PathVariable UUID storeId) {
        return ResponseEntity.ok(ApiResponse.success(stockItemService.getStockByStore(storeId)));
    }

    @GetMapping
    @Operation(summary = "Get all stock items (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<StockItemResponseDto>>> getAllStockItems(Pageable pageable) {
        Page<StockItemResponseDto> page = stockItemService.getAllStockItems(pageable);
        PageResponse<StockItemResponseDto> pageResponse = PageResponse.<StockItemResponseDto>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @PutMapping("/{id}/quantity")
    @Operation(summary = "Update stock quantity")
    public ResponseEntity<ApiResponse<StockItemResponseDto>> updateQuantity(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        return ResponseEntity
                .ok(ApiResponse.success("Quantity updated", stockItemService.updateQuantity(id, quantity)));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock (for order placement)")
    public ResponseEntity<ApiResponse<Boolean>> reserveStock(@Valid @RequestBody StockReservationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock reserved", stockItemService.reserveStock(request)));
    }

    @PostMapping("/release")
    @Operation(summary = "Release stock (for order cancellation)")
    public ResponseEntity<ApiResponse<Boolean>> releaseStock(@Valid @RequestBody StockReservationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock released", stockItemService.releaseStock(request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete stock item")
    public ResponseEntity<ApiResponse<Void>> deleteStockItem(@PathVariable UUID id) {
        stockItemService.deleteStockItem(id);
        return ResponseEntity.ok(ApiResponse.success("Stock item deleted", null));
    }
}
