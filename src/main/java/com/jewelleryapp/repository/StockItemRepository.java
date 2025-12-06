package com.jewelleryapp.repository;

import com.jewelleryapp.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, UUID>, JpaSpecificationExecutor<StockItem> {

    // Find stock for a product at a specific store
    Optional<StockItem> findByProductIdAndStoreId(UUID productId, UUID storeId);

    // Find stock for a product in Central Warehouse (Store ID is null)
    Optional<StockItem> findByProductIdAndStoreIdIsNull(UUID productId);

    // New: Find ALL locations (stores + warehouse) that have enough stock for a product
    @Query("SELECT s FROM StockItem s WHERE s.product.id = :productId AND s.quantity >= :minQuantity")
    List<StockItem> findLocationsWithStock(UUID productId, int minQuantity);
}