package com.jewellery.storeservice.config;

import com.jewellery.storeservice.entity.Store;
import com.jewellery.storeservice.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Development-only data seeder for store-service.
 * Will NOT run in production (requires "dev" profile).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (storeRepository.count() > 0) {
            log.info("Stores already exist. Skipping store seeding.");
            return;
        }

        log.info("Seeding stores...");

        storeRepository.save(Store.builder()
                .name("Main Street Showroom")
                .address("123 Main Street, Downtown, New York, NY 10001")
                .latitude(new BigDecimal("40.7128"))
                .longitude(new BigDecimal("-74.0060"))
                .operatingHours("Mon-Sat: 10AM-8PM")
                .contactPhone("555-0101")
                .email("main@jewelleryapp.com")
                .isActive(true)
                .build());

        storeRepository.save(Store.builder()
                .name("Grand Mall Branch")
                .address("456 Shopping Plaza, Level 2, Los Angeles, CA 90001")
                .latitude(new BigDecimal("34.0522"))
                .longitude(new BigDecimal("-118.2437"))
                .operatingHours("Mon-Sun: 10AM-9PM")
                .contactPhone("555-0102")
                .email("mall@jewelleryapp.com")
                .isActive(true)
                .build());

        storeRepository.save(Store.builder()
                .name("Downtown Boutique")
                .address("789 Fashion Avenue, Chicago, IL 60601")
                .latitude(new BigDecimal("41.8781"))
                .longitude(new BigDecimal("-87.6298"))
                .operatingHours("Tue-Sat: 11AM-7PM")
                .contactPhone("555-0103")
                .email("boutique@jewelleryapp.com")
                .isActive(true)
                .build());

        log.info("Store service data seeding complete. Seeded 3 stores.");
    }
}
