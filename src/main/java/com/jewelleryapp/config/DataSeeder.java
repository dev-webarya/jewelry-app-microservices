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
import java.util.HashSet;
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
        log.info("--- Starting Application Data Seeding ---");

        // 1. Initialize Essential Roles (System Foundation)
        Map<String, Role> roles = seedRoles();

        // 2. Check if Admin exists. If yes, assume system is already initialized.
        if (userRepository.findByEmail("admin@app.com").isPresent()) {
            log.info("System already seeded. Skipping initialization.");
            return;
        }

        // 3. Create Super Admin (The Architect)
        User adminUser = seedAdmin(roles.get("ROLE_ADMIN"));

        // 4. Create Global Product Catalog (Admin Responsibility)
        CatalogData catalog = seedGlobalCatalog();
        List<Product> products = seedProducts(catalog);

        // 5. Create Retail Stores (Admin Responsibility)
        Map<String, Store> stores = seedStores();

        // 6. Hire & Assign Store Managers (Admin Responsibility)
        seedStoreManagers(roles.get("ROLE_STORE_MANAGER"), stores);

        // 7. Stock Stores (Store Manager Responsibility - Simulated)
        seedInventory(products, stores);

        // 8. Onboard Customers (Public Usage)
        seedCustomers(roles.get("ROLE_CUSTOMER"));

        log.info("--- Data Seeding Completed Successfully ---");
    }

    private Map<String, Role> seedRoles() {
        log.info("[Phase 1] Seeding Roles...");
        List<String> roleNames = List.of("ROLE_CUSTOMER", "ROLE_ADMIN", "ROLE_STORE_MANAGER");
        return roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseGet(() -> roleRepository.save(Role.builder().name(name).build())))
                .collect(Collectors.toMap(Role::getName, role -> role));
    }

    private User seedAdmin(Role adminRole) {
        log.info("[Phase 1] Seeding Super Admin...");
        return userRepository.save(User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email("admin@app.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("0000000000")
                .roles(new HashSet<>(Set.of(adminRole))) // Admin has ADMIN role
                .isEnabled(true)
                .build());
    }

    private CatalogData seedGlobalCatalog() {
        log.info("[Phase 2] Seeding Global Catalog (Categories, Attributes, Collections)...");

        // --- Attributes ---
        AttributeType metal = attributeTypeRepository.save(AttributeType.builder().name("Metal").build());
        AttributeType gemstone = attributeTypeRepository.save(AttributeType.builder().name("Gemstone").build());
        AttributeType purity = attributeTypeRepository.save(AttributeType.builder().name("Purity").build());
        AttributeType size = attributeTypeRepository.save(AttributeType.builder().name("Size").build()); // Added Size

        // Metal Values
        AttributeValue gold = attributeValueRepository.save(AttributeValue.builder().attributeType(metal).value("Gold").build());
        AttributeValue silver = attributeValueRepository.save(AttributeValue.builder().attributeType(metal).value("Silver").build());
        AttributeValue roseGold = attributeValueRepository.save(AttributeValue.builder().attributeType(metal).value("Rose Gold").build());
        AttributeValue platinum = attributeValueRepository.save(AttributeValue.builder().attributeType(metal).value("Platinum").build());

        // Gemstone Values
        AttributeValue diamond = attributeValueRepository.save(AttributeValue.builder().attributeType(gemstone).value("Diamond").build());
        AttributeValue ruby = attributeValueRepository.save(AttributeValue.builder().attributeType(gemstone).value("Ruby").build());
        AttributeValue emerald = attributeValueRepository.save(AttributeValue.builder().attributeType(gemstone).value("Emerald").build());
        AttributeValue sapphire = attributeValueRepository.save(AttributeValue.builder().attributeType(gemstone).value("Sapphire").build());

        // Purity Values
        AttributeValue k14 = attributeValueRepository.save(AttributeValue.builder().attributeType(purity).value("14K").build());
        AttributeValue k18 = attributeValueRepository.save(AttributeValue.builder().attributeType(purity).value("18K").build());
        AttributeValue k22 = attributeValueRepository.save(AttributeValue.builder().attributeType(purity).value("22K").build());
        AttributeValue k24 = attributeValueRepository.save(AttributeValue.builder().attributeType(purity).value("24K").build());

        // Size Values
        AttributeValue size6 = attributeValueRepository.save(AttributeValue.builder().attributeType(size).value("6").build());
        AttributeValue size7 = attributeValueRepository.save(AttributeValue.builder().attributeType(size).value("7").build());
        AttributeValue size8 = attributeValueRepository.save(AttributeValue.builder().attributeType(size).value("8").build());

        // --- Categories ---
        Category rings = categoryRepository.save(Category.builder().name("Rings").build());
        Category necklaces = categoryRepository.save(Category.builder().name("Necklaces").build());
        Category earrings = categoryRepository.save(Category.builder().name("Earrings").build());
        Category bracelets = categoryRepository.save(Category.builder().name("Bracelets").build());

        Category weddingRings = categoryRepository.save(Category.builder().name("Wedding Rings").parent(rings).build());
        Category engagementRings = categoryRepository.save(Category.builder().name("Engagement Rings").parent(rings).build());
        Category chains = categoryRepository.save(Category.builder().name("Chains").parent(necklaces).build());
        Category pendants = categoryRepository.save(Category.builder().name("Pendants").parent(necklaces).build());

        // --- Collections ---
        Collection bridal = collectionRepository.save(Collection.builder().name("Bridal 2025").description("Wedding essentials for the modern bride").build());
        Collection festival = collectionRepository.save(Collection.builder().name("Diwali Special").description("Traditional designs for festive lights").build());
        Collection office = collectionRepository.save(Collection.builder().name("Office Chic").description("Minimalist jewelry for everyday wear").build());
        Collection luxury = collectionRepository.save(Collection.builder().name("Royal Heritage").description("Heavy, intricate designs inspired by royalty").build());

        return new CatalogData(
                gold, silver, roseGold, platinum,
                diamond, ruby, emerald, sapphire,
                k14, k18, k22, k24,
                size6, size7, size8,
                rings, weddingRings, engagementRings, necklaces, chains, pendants, earrings, bracelets,
                bridal, festival, office, luxury
        );
    }

    private List<Product> seedProducts(CatalogData c) {
        log.info("[Phase 2] Seeding Products...");
        List<Product> products = List.of(
                // 1. Wedding Ring (High Value)
                Product.builder()
                        .sku("RNG-GLD-DIA-001")
                        .name("Eternal Gold Diamond Ring")
                        .description("18K Gold ring studded with VVS diamonds. Perfect for engagements.")
                        .basePrice(new BigDecimal("1200.00"))
                        .isActive(true)
                        .category(c.engagementRings)
                        .collections(Set.of(c.bridal))
                        .attributes(Set.of(c.gold, c.diamond, c.k18, c.size7))
                        .build(),

                // 2. Silver Chain (Low Value, High Volume)
                Product.builder()
                        .sku("NCK-SIL-001")
                        .name("Modern Silver Chain")
                        .description("Sleek sterling silver chain for daily wear. Anti-tarnish coating.")
                        .basePrice(new BigDecimal("150.00"))
                        .isActive(true)
                        .category(c.chains)
                        .collections(Set.of(c.office, c.festival))
                        .attributes(Set.of(c.silver))
                        .build(),

                // 3. Traditional Band (Mid Value)
                Product.builder()
                        .sku("RNG-GLD-PLN-002")
                        .name("Traditional Gold Band")
                        .description("Plain 22K gold band, timeless design. Ideal for weddings.")
                        .basePrice(new BigDecimal("800.00"))
                        .isActive(true)
                        .category(c.weddingRings)
                        .collections(Set.of(c.bridal, c.festival))
                        .attributes(Set.of(c.gold, c.k22, c.size8))
                        .build(),

                // 4. Rose Gold Pendant (Trendy)
                Product.builder()
                        .sku("PND-RGLD-RUBY-001")
                        .name("Rose Gold Ruby Pendant")
                        .description("Delicate 14K rose gold pendant featuring a heart-shaped ruby.")
                        .basePrice(new BigDecimal("450.00"))
                        .isActive(true)
                        .category(c.pendants)
                        .collections(Set.of(c.office))
                        .attributes(Set.of(c.roseGold, c.ruby, c.k14))
                        .build(),

                // 5. Platinum Earrings (Luxury)
                Product.builder()
                        .sku("EAR-PLT-DIA-001")
                        .name("Platinum Solitaire Studs")
                        .description("Classic platinum studs with 0.5ct diamonds. A symbol of elegance.")
                        .basePrice(new BigDecimal("2500.00"))
                        .isActive(true)
                        .category(c.earrings)
                        .collections(Set.of(c.luxury, c.bridal))
                        .attributes(Set.of(c.platinum, c.diamond))
                        .build(),

                // 6. Emerald Bracelet (Statement Piece)
                Product.builder()
                        .sku("BRA-GLD-EMR-001")
                        .name("Royal Emerald Bracelet")
                        .description("Heavy 22K gold bracelet encrusted with Colombian emeralds.")
                        .basePrice(new BigDecimal("3500.00"))
                        .isActive(true)
                        .category(c.bracelets)
                        .collections(Set.of(c.luxury, c.festival))
                        .attributes(Set.of(c.gold, c.emerald, c.k22))
                        .build(),

                // 7. Sapphire Ring (Cocktail Ring)
                Product.builder()
                        .sku("RNG-SIL-SAPP-001")
                        .name("Midnight Sapphire Ring")
                        .description("Sterling silver ring with a large blue sapphire center stone.")
                        .basePrice(new BigDecimal("300.00"))
                        .isActive(true)
                        .category(c.rings)
                        .collections(Set.of(c.festival))
                        .attributes(Set.of(c.silver, c.sapphire, c.size6))
                        .build()
        );
        return productRepository.saveAll(products);
    }

    private Map<String, Store> seedStores() {
        log.info("[Phase 3] Seeding Retail Stores...");
        Store nyStore = storeRepository.save(Store.builder()
                .name("NYC Flagship Store")
                .address("5th Avenue, New York, NY")
                .contactPhone("212-555-0199")
                .operatingHours("10 AM - 9 PM")
                .build());

        Store laStore = storeRepository.save(Store.builder()
                .name("LA Boutique")
                .address("Rodeo Drive, Beverly Hills, CA")
                .contactPhone("310-555-0188")
                .operatingHours("11 AM - 8 PM")
                .build());

        Store chiStore = storeRepository.save(Store.builder()
                .name("Chicago Outlet")
                .address("Michigan Avenue, Chicago, IL")
                .contactPhone("312-555-0177")
                .operatingHours("9 AM - 6 PM")
                .build());

        return Map.of("NYC", nyStore, "LA", laStore, "CHI", chiStore);
    }

    private void seedStoreManagers(Role managerRole, Map<String, Store> stores) {
        log.info("[Phase 4] Hiring Store Managers...");

        // NYC Manager
        userRepository.save(User.builder()
                .firstName("John")
                .lastName("Manager")
                .email("manager.nyc@app.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("9876543210")
                .roles(new HashSet<>(Set.of(managerRole)))
                .managedStore(stores.get("NYC")) // Assigned to NYC Store
                .isEnabled(true)
                .build());

        // LA Manager
        userRepository.save(User.builder()
                .firstName("Sarah")
                .lastName("Manager")
                .email("manager.la@app.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("9876543211")
                .roles(new HashSet<>(Set.of(managerRole)))
                .managedStore(stores.get("LA")) // Assigned to LA Store
                .isEnabled(true)
                .build());

        // Chicago Manager
        userRepository.save(User.builder()
                .firstName("Mike")
                .lastName("Manager")
                .email("manager.chi@app.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("9876543212")
                .roles(new HashSet<>(Set.of(managerRole)))
                .managedStore(stores.get("CHI")) // Assigned to Chicago Store
                .isEnabled(true)
                .build());
    }

    private void seedInventory(List<Product> products, Map<String, Store> stores) {
        log.info("[Phase 5] Distributing Inventory to Stores...");

        // Indices from the product list above:
        // 0: Diamond Ring, 1: Silver Chain, 2: Gold Band, 3: Ruby Pendant
        // 4: Platinum Studs, 5: Emerald Bracelet, 6: Sapphire Ring

        Product diamondRing = products.get(0);
        Product silverChain = products.get(1);
        Product goldBand = products.get(2);
        Product rubyPendant = products.get(3);
        Product platinumStuds = products.get(4);
        Product emeraldBracelet = products.get(5);
        Product sapphireRing = products.get(6);

        Store nyc = stores.get("NYC");
        Store la = stores.get("LA");
        Store chi = stores.get("CHI");

        // Central Warehouse Stock (Unassigned Store) - The "Mother Lode"
        stockItemRepository.save(StockItem.builder().product(diamondRing).store(null).quantity(500).build());
        stockItemRepository.save(StockItem.builder().product(silverChain).store(null).quantity(1000).build());
        stockItemRepository.save(StockItem.builder().product(goldBand).store(null).quantity(800).build());
        stockItemRepository.save(StockItem.builder().product(platinumStuds).store(null).quantity(200).build());

        // NYC Stock (Flagship - High Inventory, Luxury Items)
        stockItemRepository.save(StockItem.builder().product(diamondRing).store(nyc).quantity(50).build());
        stockItemRepository.save(StockItem.builder().product(goldBand).store(nyc).quantity(30).build());
        stockItemRepository.save(StockItem.builder().product(platinumStuds).store(nyc).quantity(15).build()); // Only NYC has studs initially
        stockItemRepository.save(StockItem.builder().product(emeraldBracelet).store(nyc).quantity(5).build()); // Rare item

        // LA Stock (Boutique - Trendy Items)
        stockItemRepository.save(StockItem.builder().product(diamondRing).store(la).quantity(20).build());
        stockItemRepository.save(StockItem.builder().product(rubyPendant).store(la).quantity(40).build()); // LA loves Rose Gold
        stockItemRepository.save(StockItem.builder().product(silverChain).store(la).quantity(100).build());

        // Chicago Stock (Outlet - High Volume, Affordable)
        stockItemRepository.save(StockItem.builder().product(silverChain).store(chi).quantity(200).build());
        stockItemRepository.save(StockItem.builder().product(sapphireRing).store(chi).quantity(50).build());
        stockItemRepository.save(StockItem.builder().product(goldBand).store(chi).quantity(10).build());
    }

    private void seedCustomers(Role customerRole) {
        log.info("[Phase 6] Onboarding Customers...");
        userRepository.save(User.builder()
                .firstName("Alice")
                .lastName("Customer")
                .email("alice@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("1231231234")
                .roles(new HashSet<>(Set.of(customerRole)))
                .isEnabled(true)
                .build());

        userRepository.save(User.builder()
                .firstName("Bob")
                .lastName("Shopper")
                .email("bob@test.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("1231231235")
                .roles(new HashSet<>(Set.of(customerRole)))
                .isEnabled(true)
                .build());
    }

    // Helper Record for passing catalog data between phases
    private record CatalogData(
            // Values
            AttributeValue gold, AttributeValue silver, AttributeValue roseGold, AttributeValue platinum,
            AttributeValue diamond, AttributeValue ruby, AttributeValue emerald, AttributeValue sapphire,
            AttributeValue k14, AttributeValue k18, AttributeValue k22, AttributeValue k24,
            AttributeValue size6, AttributeValue size7, AttributeValue size8,
            // Categories
            Category rings, Category weddingRings, Category engagementRings,
            Category necklaces, Category chains, Category pendants,
            Category earrings, Category bracelets,
            // Collections
            Collection bridal, Collection festival, Collection office, Collection luxury
    ) {}
}