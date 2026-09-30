package com.customclothing.system.config;

import com.customclothing.system.entity.*;
import com.customclothing.system.enums.OrderStatus;
import com.customclothing.system.enums.Role;
import com.customclothing.system.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final MeasurementRepository measurementRepository;
    private final OrderRepository orderRepository;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           MeasurementRepository measurementRepository,
                           OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.measurementRepository = measurementRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            seedUsersAndData();
            ensureInrPrices();
        } catch (Exception e) {
            log.warn("Data initialization skipped or already present: {}", e.getMessage());
        }
    }

    private void seedUsersAndData() {
        if (userRepository.count() > 0 && categoryRepository.count() > 0) {
            log.info("Database already populated. Skipping initial data seeding.");
            return;
        }

        log.info("Seeding initial demonstration data for Custom Clothing System...");

        // 1. Users
        User admin = userRepository.findByEmail("admin@customclothing.com").orElseGet(() ->
                userRepository.save(new User(
                        "Alexander Wright",
                        "admin@customclothing.com",
                        "admin123",
                        "+1 (555) 019-2831",
                        "Savile Row Atelier HQ, London",
                        Role.ADMIN
                ))
        );

        User tailorMarcus = userRepository.findByEmail("tailor.marcus@customclothing.com").orElseGet(() ->
                userRepository.save(new User(
                        "Marcus Vance",
                        "tailor.marcus@customclothing.com",
                        "tailor123",
                        "+1 (555) 014-9922",
                        "Workshop Bay 1, London",
                        Role.TAILOR
                ))
        );

        User tailorElena = userRepository.findByEmail("tailor.elena@customclothing.com").orElseGet(() ->
                userRepository.save(new User(
                        "Elena Rostova",
                        "tailor.elena@customclothing.com",
                        "tailor123",
                        "+1 (555) 017-3388",
                        "Workshop Bay 2, London",
                        Role.TAILOR
                ))
        );

        User customerDavid = userRepository.findByEmail("customer@example.com").orElseGet(() ->
                userRepository.save(new User(
                        "David Sterling",
                        "customer@example.com",
                        "customer123",
                        "+1 (555) 018-7711",
                        "742 Evergreen Terrace, Springfield",
                        Role.CUSTOMER
                ))
        );

        // 2. Categories
        Category catSuits = categoryRepository.findByNameIgnoreCase("Suits & Tuxedos").orElseGet(() ->
                categoryRepository.save(new Category(
                        "Suits & Tuxedos",
                        "Handcrafted two-piece and three-piece bespoke suits, bespoke tuxedos, and morning coats tailored to perfection.",
                        "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80"
                ))
        );

        Category catShirts = categoryRepository.findByNameIgnoreCase("Shirts & Tops").orElseGet(() ->
                categoryRepository.save(new Category(
                        "Shirts & Tops",
                        "Custom tailored dress shirts, formal French-cuff shirts, and lightweight luxury linen tops.",
                        "https://images.unsplash.com/photo-1620012253295-c15c429f66a6?auto=format&fit=crop&w=800&q=80"
                ))
        );

        Category catTrousers = categoryRepository.findByNameIgnoreCase("Trousers & Chinos").orElseGet(() ->
                categoryRepository.save(new Category(
                        "Trousers & Chinos",
                        "Precision-cut formal trousers, Italian wool pleated slacks, and casual bespoke cotton chinos.",
                        "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?auto=format&fit=crop&w=800&q=80"
                ))
        );

        Category catBlazers = categoryRepository.findByNameIgnoreCase("Blazers & Jackets").orElseGet(() ->
                categoryRepository.save(new Category(
                        "Blazers & Jackets",
                        "Structured double-breasted blazers, tweed overcoats, and modern unstructured summer sport coats.",
                        "https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=800&q=80"
                ))
        );

        Category catTraditional = categoryRepository.findByNameIgnoreCase("Traditional & Ethnic").orElseGet(() ->
                categoryRepository.save(new Category(
                        "Traditional & Ethnic",
                        "Bespoke ceremonial sherwanis, embroidered bandhgalas, raw silk kurtas, and traditional evening jackets.",
                        "https://images.unsplash.com/photo-1610030469983-98e550d6193c?auto=format&fit=crop&w=800&q=80"
                ))
        );

        Category catDresses = categoryRepository.findByNameIgnoreCase("Dresses & Gowns").orElseGet(() ->
                categoryRepository.save(new Category(
                        "Dresses & Gowns",
                        "Made-to-order evening gowns, cocktail silhouettes, and tailored architectural power dresses.",
                        "https://images.unsplash.com/photo-1566174053879-31528523f8ae?auto=format&fit=crop&w=800&q=80"
                ))
        );

        // 3. Products with realistic Indian Rupee (INR) pricing
        if (productRepository.count() == 0) {
            Product p1 = new Product(
                    "Midnight Navy Italian Wool Two-Piece Suit",
                    "Spun from Super 130s Italian Merino wool with hand-finished pick stitching, natural horn buttons, and cupro lining. Fully canvassed for an impeccable drape.",
                    28999.00,
                    catSuits,
                    "38R, 40R, 42R, 44R, Custom Made-to-Measure",
                    "Midnight Navy, Charcoal Grey, Royal Blue, Obsidian Black",
                    "Super 130s Merino Wool, Cashmere Blend, Silk Wool Jacquard",
                    "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=800&q=80"
            );

            Product p2 = new Product(
                    "Black Tie Peak Lapel Tuxedo",
                    "Classic formal black-tie tuxedo crafted with satin silk grosgrain lapels, satin-covered buttons, and matching pleated trousers.",
                    34999.00,
                    catSuits,
                    "36R, 38R, 40R, 42R, Custom Made-to-Measure",
                    "Classic Tuxedo Black, Midnight Blue",
                    "Wool Silk Barathea, British Worsted Wool",
                    "https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=800&q=80"
            );

            Product p3 = new Product(
                    "Royal Egyptian Giza 87 Poplin Dress Shirt",
                    "Hand-cut bespoke dress shirt made with long-staple Egyptian Giza 87 cotton. Features genuine mother-of-pearl buttons, split back yoke, and removable brass collar stays.",
                    4499.00,
                    catShirts,
                    "15 Neck / 33 Sleeve, 15.5 / 34, 16 / 35, 16.5 / 36, Custom Made-to-Measure",
                    "Crisp White, Ice Blue, French Pink, Lavender Micro-Stripe",
                    "100% Egyptian Giza 87 Cotton, Royal Oxford, Sea Island Cotton",
                    "https://images.unsplash.com/photo-1620012253295-c15c429f66a6?auto=format&fit=crop&w=800&q=80"
            );

            Product p4 = new Product(
                    "Riviera Pure Belgian Linen Resort Shirt",
                    "Breathable, lightweight relaxed-collar resort shirt woven from flax linen sourced from Belgium. Soft-washed for an effortless summer drape.",
                    3799.00,
                    catShirts,
                    "S, M, L, XL, XXL, Custom Made-to-Measure",
                    "Sand Beige, Mediterranean Azure, Olive Green, Natural White",
                    "Pure Belgian Flax Linen, Linen-Cotton Blend",
                    "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?auto=format&fit=crop&w=800&q=80"
            );

            Product p5 = new Product(
                    "Sartorial Pleated Wool Flannel Trousers",
                    "Double forward pleats with side adjusters, extended waistband tab, and 2-inch cuff turn-ups. Cut for superior comfort and timeless drape.",
                    6999.00,
                    catTrousers,
                    "30W, 32W, 34W, 36W, 38W, Custom Made-to-Measure",
                    "Charcoal Heather, Warm Camel, Navy Blue, Houndstooth Grey",
                    "British Wool Flannel, Tropical Wool Worsted, Stretch Twill",
                    "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?auto=format&fit=crop&w=800&q=80"
            );

            Product p6 = new Product(
                    "Heritage Double-Breasted Camel Wool Blazer",
                    "A timeless outerwear statement crafted with 6x2 gold crest buttons, broad peak lapels, and double vented silhouette.",
                    18499.00,
                    catBlazers,
                    "38R, 40R, 42R, 44R, Custom Made-to-Measure",
                    "Camel Tan, Forest Green, Deep Burgundy, Navy",
                    "Pure Camel Hair Wool, Cashmere Tweed, Wool Hopsack",
                    "https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?auto=format&fit=crop&w=800&q=80"
            );

            Product p7 = new Product(
                    "Royal Hand-Embroidered Silk Sherwani",
                    "Exquisite ceremonial sherwani embellished with intricate zari needlework, mandarin collar, and concealed mother-of-pearl placket.",
                    25999.00,
                    catTraditional,
                    "38, 40, 42, 44, Custom Made-to-Measure",
                    "Ivory Gold, Royal Emerald, Deep Crimson, Midnight Black",
                    "Raw Benarasi Silk, Velvet Brocade, Tussar Silk",
                    "https://images.unsplash.com/photo-1610030469983-98e550d6193c?auto=format&fit=crop&w=800&q=80"
            );

            Product p8 = new Product(
                    "Emerald Velvet Couture Evening Gown",
                    "A sculpted floor-length evening silhouette crafted in plush silk velvet with draped cowl neck and fitted corset waist.",
                    16999.00,
                    catDresses,
                    "US 2, US 4, US 6, US 8, US 10, Custom Made-to-Measure",
                    "Emerald Green, Midnight Sapphire, Ruby Red, Noir",
                    "Silk Velvet, Silk Crepe de Chine, Duchesse Satin",
                    "https://images.unsplash.com/photo-1566174053879-31528523f8ae?auto=format&fit=crop&w=800&q=80"
            );

            productRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7, p8));
        }

        // 4. Sample Measurement Profile for David Sterling
        List<Measurement> davidMeasurements = measurementRepository.findByCustomerId(customerDavid.getId());
        Measurement defaultFit;
        if (davidMeasurements.isEmpty()) {
            defaultFit = measurementRepository.save(new Measurement(
                    customerDavid,
                    "David's Executive Fit",
                    41.5, // chest
                    34.0, // waist
                    41.0, // hips
                    18.5, // shoulder
                    25.5, // sleeve
                    30.5, // shirt length
                    32.0, // inseam
                    16.0, // neck
                    "inches",
                    "Prefers a fitted modern taper through torso, slight break on trouser hem, double vents."
            ));
        } else {
            defaultFit = davidMeasurements.get(0);
        }

        // 5. Sample Order for Demonstration
        if (orderRepository.count() == 0) {
            Product suit = productRepository.findAll().stream()
                    .filter(p -> p.getName().contains("Midnight Navy"))
                    .findFirst().orElse(null);

            if (suit != null) {
                Order order = new Order(
                        "CCO-2026-BESPOKE1",
                        customerDavid,
                        suit.getBasePrice(),
                        "742 Evergreen Terrace, Springfield, IL 62704",
                        "+1 (555) 018-7711",
                        "Monogram initial 'DS' in navy cursive on inside chest pocket."
                );
                order.setStatus(OrderStatus.UNDER_STITCHING);
                order.setAssignedTailor(tailorMarcus);
                order.setExpectedDeliveryDate(LocalDateTime.now().plusDays(7));
                order.setTailorNotes("Fabric inspected and cold-steamed. Canvassing basted, shoulder pads shaped.");

                OrderItem item = new OrderItem(
                        order,
                        suit,
                        1,
                        suit.getBasePrice(),
                        "Custom Made-to-Measure",
                        "Midnight Navy",
                        "Super 130s Merino Wool",
                        defaultFit,
                        "Monogram 'DS' inside pocket; functional cuff buttonholes."
                );
                order.addOrderItem(item);
                orderRepository.save(order);
            }
        }

        log.info("Demonstration data seeded successfully!");
    }

    /**
     * Ensures all products and demo orders in existing database use realistic Indian Rupee (INR) values.
     */
    private void ensureInrPrices() {
        java.util.Map<String, Double> inrPrices = java.util.Map.of(
                "Midnight Navy Italian Wool Two-Piece Suit", 28999.00,
                "Black Tie Peak Lapel Tuxedo", 34999.00,
                "Royal Egyptian Giza 87 Poplin Dress Shirt", 4499.00,
                "Riviera Pure Belgian Linen Resort Shirt", 3799.00,
                "Sartorial Pleated Wool Flannel Trousers", 6999.00,
                "Heritage Double-Breasted Camel Wool Blazer", 18499.00,
                "Royal Hand-Embroidered Silk Sherwani", 25999.00,
                "Emerald Velvet Couture Evening Gown", 16999.00
        );

        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            Double targetPrice = inrPrices.get(product.getName());
            if (targetPrice != null && !targetPrice.equals(product.getBasePrice())) {
                log.info("Migrating product '{}' price from {} to INR {}", product.getName(), product.getBasePrice(), targetPrice);
                product.setBasePrice(targetPrice);
                productRepository.save(product);
            }
        }

        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            if (order.getTotalAmount() != null && order.getTotalAmount() < 1000.0) {
                double recalculated = 0.0;
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        if (item.getProduct() != null && inrPrices.containsKey(item.getProduct().getName())) {
                            double price = inrPrices.get(item.getProduct().getName());
                            item.setPrice(price);
                            recalculated += price * (item.getQuantity() != null ? item.getQuantity() : 1);
                        }
                    }
                }
                if (recalculated > 0.0) {
                    log.info("Migrating order '{}' total amount from {} to INR {}", order.getOrderTrackingNumber(), order.getTotalAmount(), recalculated);
                    order.setTotalAmount(recalculated);
                    orderRepository.save(order);
                }
            }
        }
    }
}
