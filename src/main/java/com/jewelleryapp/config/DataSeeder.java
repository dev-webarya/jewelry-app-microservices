package com.jewelleryapp.config;

import com.jewelleryapp.entity.*;
import com.jewelleryapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final AttributeTypeRepository attributeTypeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Seed Roles
        log.info("Checking for default roles...");
        List<String> requiredRoles = List.of("ROLE_CUSTOMER", "ROLE_ADMIN", "ROLE_STORE_MANAGER");
        Map<String, Role> rolesMap = requiredRoles.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build())))
                .collect(Collectors.toMap(Role::getName, role -> role));
        log.info("Roles check complete.");

        // 2. Check if database is already seeded
        if (userRepository.count() > 0) {
            log.info("Database already contains users. Skipping bulk data seeding.");
            return;
        }

        log.info("No users found. Proceeding with bulk database seeding...");

        // 3. Seed Stores
        Map<String, Store> stores = seedStores();
        Store mainStore = stores.get("main");
        Store mallStore = stores.get("mall");

        // 4. Seed Users (Admin & Store Managers)
        seedUsers(rolesMap);

        // 5. Seed Full Catalog (Attributes, Categories, Collections)
        CatalogData catalog = seedCatalog();

        // 6. Seed Bulk Products
        List<Product> products = seedProducts(catalog);

        // 7. Seed Stock for all products
        seedStock(products, mainStore, mallStore);

        log.info("Database bulk seeding complete.");
    }

    private Map<String, Store> seedStores() {
        log.info("Seeding stores...");
        Store mainStore = storeRepository.save(Store.builder()
                .name("Main Street Showroom")
                .address("123 Main Street, Downtown, New York, NY 10001")
                .latitude(new BigDecimal("40.7128"))
                .longitude(new BigDecimal("-74.0060"))
                .operatingHours("Mon-Sat: 10AM-8PM")
                .contactPhone("555-0101")
                .build());

        Store mallStore = storeRepository.save(Store.builder()
                .name("Grand Mall Branch")
                .address("456 Shopping Plaza, Level 2, Los Angeles, CA 90001")
                .latitude(new BigDecimal("34.0522"))
                .longitude(new BigDecimal("-118.2437"))
                .operatingHours("Mon-Sun: 10AM-9PM")
                .contactPhone("555-0102")
                .build());

        Store boutiqueStore = storeRepository.save(Store.builder()
                .name("Downtown Boutique")
                .address("789 Fashion Avenue, Chicago, IL 60601")
                .latitude(new BigDecimal("41.8781"))
                .longitude(new BigDecimal("-87.6298"))
                .operatingHours("Tue-Sat: 11AM-7PM")
                .contactPhone("555-0103")
                .build());

        return Map.of("main", mainStore, "mall", mallStore, "boutique", boutiqueStore);
    }

    private void seedUsers(Map<String, Role> roles) {
        log.info("Seeding users (Admin and Store Managers)...");
        // Admin User
        userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@app.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("9999999999")
                .roles(Set.of(roles.get("ROLE_ADMIN"), roles.get("ROLE_CUSTOMER")))
                .isEnabled(true)
                .build());

        // Store Manager for Main Street
        userRepository.save(User.builder()
                .firstName("Main")
                .lastName("Manager")
                .email("manager.main@app.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("8888888888")
                .roles(Set.of(roles.get("ROLE_STORE_MANAGER"), roles.get("ROLE_CUSTOMER")))
                .isEnabled(true)
                .build());

        // Store Manager for Mall Branch
        userRepository.save(User.builder()
                .firstName("Mall")
                .lastName("Manager")
                .email("manager.mall@app.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("7777777777")
                .roles(Set.of(roles.get("ROLE_STORE_MANAGER"), roles.get("ROLE_CUSTOMER")))
                .isEnabled(true)
                .build());
    }

    private CatalogData seedCatalog() {
        log.info("Seeding full catalog (attributes, categories, collections)...");

        // Attribute Types
        AttributeType metalType = attributeTypeRepository.save(AttributeType.builder().name("Metal Type").build());
        AttributeType gemstoneType = attributeTypeRepository.save(AttributeType.builder().name("Gemstone").build());
        AttributeType purityType = attributeTypeRepository.save(AttributeType.builder().name("Purity").build());
        AttributeType sizeType = attributeTypeRepository.save(AttributeType.builder().name("Size").build());
        AttributeType styleType = attributeTypeRepository.save(AttributeType.builder().name("Style").build());

        // Attribute Values
        AttributeValue gold = attributeValueRepository.save(AttributeValue.builder().attributeType(metalType).value("Gold").build());
        AttributeValue silver = attributeValueRepository.save(AttributeValue.builder().attributeType(metalType).value("Silver").build());
        AttributeValue platinum = attributeValueRepository.save(AttributeValue.builder().attributeType(metalType).value("Platinum").build());
        AttributeValue roseGold = attributeValueRepository.save(AttributeValue.builder().attributeType(metalType).value("Rose Gold").build());

        AttributeValue diamond = attributeValueRepository.save(AttributeValue.builder().attributeType(gemstoneType).value("Diamond").build());
        AttributeValue ruby = attributeValueRepository.save(AttributeValue.builder().attributeType(gemstoneType).value("Ruby").build());
        AttributeValue emerald = attributeValueRepository.save(AttributeValue.builder().attributeType(gemstoneType).value("Emerald").build());

        AttributeValue purity14k = attributeValueRepository.save(AttributeValue.builder().attributeType(purityType).value("14K").build());
        AttributeValue purity18k = attributeValueRepository.save(AttributeValue.builder().attributeType(purityType).value("18K").build());
        AttributeValue purity22k = attributeValueRepository.save(AttributeValue.builder().attributeType(purityType).value("22K").build());

        AttributeValue sizeSmall = attributeValueRepository.save(AttributeValue.builder().attributeType(sizeType).value("Small").build());
        AttributeValue sizeMedium = attributeValueRepository.save(AttributeValue.builder().attributeType(sizeType).value("Medium").build());
        AttributeValue sizeLarge = attributeValueRepository.save(AttributeValue.builder().attributeType(sizeType).value("Large").build());

        AttributeValue styleTraditional = attributeValueRepository.save(AttributeValue.builder().attributeType(styleType).value("Traditional").build());
        AttributeValue styleContemporary = attributeValueRepository.save(AttributeValue.builder().attributeType(styleType).value("Contemporary").build());
        AttributeValue styleVintage = attributeValueRepository.save(AttributeValue.builder().attributeType(styleType).value("Vintage").build());


        // Categories
        Category rings = categoryRepository.save(Category.builder().name("Rings").build());
        Category engagementRings = categoryRepository.save(Category.builder().name("Engagement Rings").parent(rings).build());
        Category weddingBands = categoryRepository.save(Category.builder().name("Wedding Bands").parent(rings).build());
        Category fashionRings = categoryRepository.save(Category.builder().name("Fashion Rings").parent(rings).build());

        Category necklaces = categoryRepository.save(Category.builder().name("Necklaces").build());
        Category pendants = categoryRepository.save(Category.builder().name("Pendants").parent(necklaces).build());
        Category chains = categoryRepository.save(Category.builder().name("Chains").parent(necklaces).build());

        Category bangles = categoryRepository.save(Category.builder().name("Bangles").build());
        Category traditionalBangles = categoryRepository.save(Category.builder().name("Traditional Bangles").parent(bangles).build());
        Category cuffBangles = categoryRepository.save(Category.builder().name("Cuff Bangles").parent(bangles).build());

        // Collections
        Collection bridal = collectionRepository.save(Collection.builder().name("Bridal Collection").description("Exclusive designs for your special day").build());
        Collection summer = collectionRepository.save(Collection.builder().name("Summer 2025").description("Bright and vibrant summer designs").build());
        Collection festival = collectionRepository.save(Collection.builder().name("Festival Special").description("Traditional jewelry for festivals").build());
        Collection office = collectionRepository.save(Collection.builder().name("Office Wear").description("Subtle and elegant pieces").build());

        return new CatalogData(
                gold, silver, platinum, roseGold,
                diamond, ruby, emerald,
                purity14k, purity18k, purity22k,
                sizeSmall, sizeMedium, sizeLarge,
                styleTraditional, styleContemporary, styleVintage,
                rings, engagementRings, weddingBands, fashionRings,
                necklaces, pendants, chains,
                bangles, traditionalBangles, cuffBangles,
                bridal, summer, festival, office
        );
    }

    private List<Product> seedProducts(CatalogData c) {
        log.info("Seeding bulk products...");

        List<Product> products = List.of(
                // Rings
                Product.builder()
                        .sku("RING-G-001")
                        .name("Classic Gold Ring")
                        .description("Timeless 14K gold ring with elegant design")
                        .basePrice(new BigDecimal("499.99"))
                        .isActive(true)
                        .category(c.fashionRings)
                        .collections(Set.of(c.office))
                        .attributes(Set.of(c.gold, c.purity14k, c.sizeMedium, c.styleTraditional))
                        .build(),
                Product.builder()
                        .sku("RING-P-ENG-001")
                        .name("Platinum Diamond Engagement Ring")
                        .description("Stunning platinum ring with 1 carat diamond")
                        .basePrice(new BigDecimal("2999.99"))
                        .isActive(true)
                        .category(c.engagementRings)
                        .collections(Set.of(c.bridal))
                        .attributes(Set.of(c.platinum, c.diamond, c.sizeMedium, c.styleContemporary))
                        .build(),
                Product.builder()
                        .sku("RING-RG-WED-001")
                        .name("Rose Gold Wedding Band")
                        .description("Beautiful 18K rose gold wedding band")
                        .basePrice(new BigDecimal("899.99"))
                        .isActive(true)
                        .category(c.weddingBands)
                        .collections(Set.of(c.bridal))
                        .attributes(Set.of(c.roseGold, c.purity18k, c.sizeLarge, c.styleContemporary))
                        .build(),
                Product.builder()
                        .sku("RING-S-FASH-001")
                        .name("Contemporary Silver Ring")
                        .description("Modern silver ring with minimalist design")
                        .basePrice(new BigDecimal("199.99"))
                        .isActive(true)
                        .category(c.fashionRings)
                        .collections(Set.of(c.summer, c.office))
                        .attributes(Set.of(c.silver, c.sizeSmall, c.styleContemporary))
                        .build(),
                Product.builder()
                        .sku("RING-G-RUBY-001")
                        .name("Gold Ring with Ruby")
                        .description("Elegant 18K gold ring with ruby gemstone")
                        .basePrice(new BigDecimal("1599.99"))
                        .isActive(true)
                        .category(c.fashionRings)
                        .collections(Set.of(c.festival))
                        .attributes(Set.of(c.gold, c.ruby, c.purity18k, c.sizeMedium, c.styleTraditional))
                        .build(),

                // Necklaces
                Product.builder()
                        .sku("NECK-G-DIA-001")
                        .name("Diamond Pendant Necklace")
                        .description("Stunning 18K gold necklace with diamond pendant")
                        .basePrice(new BigDecimal("2499.99"))
                        .isActive(true)
                        .category(c.pendants)
                        .collections(Set.of(c.bridal, c.festival))
                        .attributes(Set.of(c.gold, c.diamond, c.purity18k, c.styleTraditional))
                        .build(),
                Product.builder()
                        .sku("NECK-G-CHAIN-001")
                        .name("22K Gold Chain Necklace")
                        .description("Traditional 22K gold chain, perfect for everyday wear")
                        .basePrice(new BigDecimal("3999.99"))
                        .isActive(true)
                        .category(c.chains)
                        .collections(Set.of(c.festival))
                        .attributes(Set.of(c.gold, c.purity22k, c.styleTraditional))
                        .build(),
                Product.builder()
                        .sku("NECK-S-PEND-001")
                        .name("Contemporary Silver Pendant")
                        .description("Modern silver pendant necklace with sleek design")
                        .basePrice(new BigDecimal("399.99"))
                        .isActive(true)
                        .category(c.pendants)
                        .collections(Set.of(c.summer, c.office))
                        .attributes(Set.of(c.silver, c.styleContemporary))
                        .build(),
                Product.builder()
                        .sku("NECK-G-EMR-001")
                        .name("Emerald Pendant Necklace")
                        .description("Exquisite 18K gold necklace with emerald gemstone")
                        .basePrice(new BigDecimal("1899.99"))
                        .isActive(true)
                        .category(c.pendants)
                        .collections(Set.of(c.festival))
                        .attributes(Set.of(c.gold, c.emerald, c.purity18k, c.styleVintage))
                        .build(),

                // Bangles
                Product.builder()
                        .sku("BANG-G-TRAD-001")
                        .name("Traditional 22K Gold Bangle")
                        .description("Heavy traditional 22K gold bangle with intricate design")
                        .basePrice(new BigDecimal("5999.99"))
                        .isActive(true)
                        .category(c.traditionalBangles)
                        .collections(Set.of(c.festival, c.bridal))
                        .attributes(Set.of(c.gold, c.purity22k, c.sizeMedium, c.styleTraditional))
                        .build(),
                Product.builder()
                        .sku("BANG-S-CUFF-001")
                        .name("Contemporary Silver Cuff Bangle")
                        .description("Modern silver cuff bangle with sleek design")
                        .basePrice(new BigDecimal("299.99"))
                        .isActive(true)
                        .category(c.cuffBangles)
                        .collections(Set.of(c.summer, c.office))
                        .attributes(Set.of(c.silver, c.sizeLarge, c.styleContemporary))
                        .build(),
                Product.builder()
                        .sku("BANG-G-DIA-SET-001")
                        .name("Diamond Bangle Set (Pair)")
                        .description("Pair of 18K gold bangles with diamond accents")
                        .basePrice(new BigDecimal("7999.99"))
                        .isActive(true)
                        .category(c.traditionalBangles)
                        .collections(Set.of(c.bridal))
                        .attributes(Set.of(c.gold, c.diamond, c.purity18k, c.sizeMedium, c.styleContemporary))
                        .build()
        );

        return productRepository.saveAll(products);
    }

    private void seedStock(List<Product> products, Store mainStore, Store mallStore) {
        log.info("Seeding stock for all products...");

        // Add central warehouse stock for every product
        for (Product product : products) {
            stockItemRepository.save(StockItem.builder()
                    .product(product)
                    .store(null) // null storeId means central warehouse
                    .quantity(100) // Default 100 in central
                    .build());
        }

        // Add specific local stock for a few items
        stockItemRepository.save(StockItem.builder()
                .product(products.get(0)) // Classic Gold Ring
                .store(mainStore)
                .quantity(25)
                .build());

        stockItemRepository.save(StockItem.builder()
                .product(products.get(0)) // Classic Gold Ring
                .store(mallStore)
                .quantity(15)
                .build());

        stockItemRepository.save(StockItem.builder()
                .product(products.get(1)) // Platinum Engagement Ring
                .store(mainStore)
                .quantity(10)
                .build());

        stockItemRepository.save(StockItem.builder()
                .product(products.get(10)) // Silver Cuff Bangle
                .store(mallStore)
                .quantity(30)
                .build());
    }

    // Helper record to pass catalog data
    private record CatalogData(
            // Values
            AttributeValue gold, AttributeValue silver, AttributeValue platinum, AttributeValue roseGold,
            AttributeValue diamond, AttributeValue ruby, AttributeValue emerald,
            AttributeValue purity14k, AttributeValue purity18k, AttributeValue purity22k,
            AttributeValue sizeSmall, AttributeValue sizeMedium, AttributeValue sizeLarge,
            AttributeValue styleTraditional, AttributeValue styleContemporary, AttributeValue styleVintage,
            // Categories
            Category rings, Category engagementRings, Category weddingBands, Category fashionRings,
            Category necklaces, Category pendants, Category chains,
            Category bangles, Category traditionalBangles, Category cuffBangles,
            // Collections
            Collection bridal, Collection summer, Collection festival, Collection office
    ) {}
}