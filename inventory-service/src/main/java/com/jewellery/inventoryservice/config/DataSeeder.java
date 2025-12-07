package com.jewellery.inventoryservice.config;

import com.jewellery.inventoryservice.entity.StockItem;
import com.jewellery.inventoryservice.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Development-only data seeder for inventory-service.
 * Seeds sample stock items.
 * Will NOT run in production (requires "dev" profile).
 * 
 * Note: In microservices, products are managed by product-service.
 * This seeder uses hardcoded product UUIDs for demo purposes.
 * In real use, stock would be created via API after products exist.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final StockItemRepository stockItemRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (stockItemRepository.count() > 0) {
            log.info("Stock items already exist. Skipping inventory seeding.");
            return;
        }

        log.info("Seeding sample stock items...");

        // Create sample stock items with random product IDs
        // In a real scenario, these would match actual product IDs from product-service

        // Sample central warehouse stock (storeId = null)
        for (int i = 0; i < 5; i++) {
            stockItemRepository.save(StockItem.builder()
                    .productId(UUID.randomUUID())
                    .storeId(null) // Central warehouse
                    .quantity(100)
                    .build());
        }

        log.info("Inventory service data seeding complete. Seeded 5 sample stock items.");
        log.info("Note: Use product-service product IDs and POST to /api/v1/stock-items to create real stock.");
    }
}
