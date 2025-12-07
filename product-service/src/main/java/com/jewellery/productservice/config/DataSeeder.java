package com.jewellery.productservice.config;

import com.jewellery.productservice.entity.Category;
import com.jewellery.productservice.entity.Collection;
import com.jewellery.productservice.entity.Product;
import com.jewellery.productservice.repository.CategoryRepository;
import com.jewellery.productservice.repository.CollectionRepository;
import com.jewellery.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Development-only data seeder for product-service.
 * Seeds categories, collections, and sample products.
 * Will NOT run in production (requires "dev" profile).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("Products already exist. Skipping product seeding.");
            return;
        }

        log.info("Seeding product catalog...");

        // Seed Categories
        Category rings = categoryRepository.save(Category.builder().name("Rings").build());
        Category engagementRings = categoryRepository
                .save(Category.builder().name("Engagement Rings").parent(rings).build());
        Category weddingBands = categoryRepository.save(Category.builder().name("Wedding Bands").parent(rings).build());
        Category fashionRings = categoryRepository.save(Category.builder().name("Fashion Rings").parent(rings).build());

        Category necklaces = categoryRepository.save(Category.builder().name("Necklaces").build());
        Category pendants = categoryRepository.save(Category.builder().name("Pendants").parent(necklaces).build());
        Category chains = categoryRepository.save(Category.builder().name("Chains").parent(necklaces).build());

        Category bangles = categoryRepository.save(Category.builder().name("Bangles").build());
        Category traditionalBangles = categoryRepository
                .save(Category.builder().name("Traditional Bangles").parent(bangles).build());
        Category cuffBangles = categoryRepository.save(Category.builder().name("Cuff Bangles").parent(bangles).build());

        log.info("Seeded 10 categories.");

        // Seed Collections
        Collection bridal = collectionRepository.save(Collection.builder()
                .name("Bridal Collection")
                .description("Exclusive designs for your special day")
                .build());
        Collection summer = collectionRepository.save(Collection.builder()
                .name("Summer 2025")
                .description("Bright and vibrant summer designs")
                .build());
        Collection festival = collectionRepository.save(Collection.builder()
                .name("Festival Special")
                .description("Traditional jewelry for festivals")
                .build());
        Collection office = collectionRepository.save(Collection.builder()
                .name("Office Wear")
                .description("Subtle and elegant pieces")
                .build());

        log.info("Seeded 4 collections.");

        // Seed Products
        productRepository.save(Product.builder()
                .sku("RING-G-001")
                .name("Classic Gold Ring")
                .description("Timeless 14K gold ring with elegant design")
                .basePrice(new BigDecimal("499.99"))
                .isActive(true)
                .category(fashionRings)
                .collections(Set.of(office))
                .build());

        productRepository.save(Product.builder()
                .sku("RING-P-ENG-001")
                .name("Platinum Diamond Engagement Ring")
                .description("Stunning platinum ring with 1 carat diamond")
                .basePrice(new BigDecimal("2999.99"))
                .isActive(true)
                .category(engagementRings)
                .collections(Set.of(bridal))
                .build());

        productRepository.save(Product.builder()
                .sku("RING-RG-WED-001")
                .name("Rose Gold Wedding Band")
                .description("Beautiful 18K rose gold wedding band")
                .basePrice(new BigDecimal("899.99"))
                .isActive(true)
                .category(weddingBands)
                .collections(Set.of(bridal))
                .build());

        productRepository.save(Product.builder()
                .sku("RING-S-FASH-001")
                .name("Contemporary Silver Ring")
                .description("Modern silver ring with minimalist design")
                .basePrice(new BigDecimal("199.99"))
                .isActive(true)
                .category(fashionRings)
                .collections(Set.of(summer, office))
                .build());

        productRepository.save(Product.builder()
                .sku("RING-G-RUBY-001")
                .name("Gold Ring with Ruby")
                .description("Elegant 18K gold ring with ruby gemstone")
                .basePrice(new BigDecimal("1599.99"))
                .isActive(true)
                .category(fashionRings)
                .collections(Set.of(festival))
                .build());

        productRepository.save(Product.builder()
                .sku("NECK-G-DIA-001")
                .name("Diamond Pendant Necklace")
                .description("Stunning 18K gold necklace with diamond pendant")
                .basePrice(new BigDecimal("2499.99"))
                .isActive(true)
                .category(pendants)
                .collections(Set.of(bridal, festival))
                .build());

        productRepository.save(Product.builder()
                .sku("NECK-G-CHAIN-001")
                .name("22K Gold Chain Necklace")
                .description("Traditional 22K gold chain, perfect for everyday wear")
                .basePrice(new BigDecimal("3999.99"))
                .isActive(true)
                .category(chains)
                .collections(Set.of(festival))
                .build());

        productRepository.save(Product.builder()
                .sku("NECK-S-PEND-001")
                .name("Contemporary Silver Pendant")
                .description("Modern silver pendant necklace with sleek design")
                .basePrice(new BigDecimal("399.99"))
                .isActive(true)
                .category(pendants)
                .collections(Set.of(summer, office))
                .build());

        productRepository.save(Product.builder()
                .sku("NECK-G-EMR-001")
                .name("Emerald Pendant Necklace")
                .description("Exquisite 18K gold necklace with emerald gemstone")
                .basePrice(new BigDecimal("1899.99"))
                .isActive(true)
                .category(pendants)
                .collections(Set.of(festival))
                .build());

        productRepository.save(Product.builder()
                .sku("BANG-G-TRAD-001")
                .name("Traditional 22K Gold Bangle")
                .description("Heavy traditional 22K gold bangle with intricate design")
                .basePrice(new BigDecimal("5999.99"))
                .isActive(true)
                .category(traditionalBangles)
                .collections(Set.of(festival, bridal))
                .build());

        productRepository.save(Product.builder()
                .sku("BANG-S-CUFF-001")
                .name("Contemporary Silver Cuff Bangle")
                .description("Modern silver cuff bangle with sleek design")
                .basePrice(new BigDecimal("299.99"))
                .isActive(true)
                .category(cuffBangles)
                .collections(Set.of(summer, office))
                .build());

        productRepository.save(Product.builder()
                .sku("BANG-G-DIA-SET-001")
                .name("Diamond Bangle Set (Pair)")
                .description("Pair of 18K gold bangles with diamond accents")
                .basePrice(new BigDecimal("7999.99"))
                .isActive(true)
                .category(traditionalBangles)
                .collections(Set.of(bridal))
                .build());

        log.info("Product service data seeding complete. Seeded 12 products.");
    }
}
