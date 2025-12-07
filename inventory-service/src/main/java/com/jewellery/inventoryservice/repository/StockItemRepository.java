package com.jewellery.inventoryservice.repository;

import com.jewellery.inventoryservice.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, UUID> {
    Optional<StockItem> findByProductIdAndStoreId(UUID productId, UUID storeId);

    Optional<StockItem> findByProductIdAndStoreIdIsNull(UUID productId);

    List<StockItem> findByProductId(UUID productId);

    List<StockItem> findByStoreId(UUID storeId);
}
