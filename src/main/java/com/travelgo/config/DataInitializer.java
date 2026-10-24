package com.travelgo.config;

import com.travelgo.entity.*;
import com.travelgo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Data initializer that seeds the database on application startup.
 * Creates default roles, permissions, admin account, system settings,
 * default staff/customer test accounts, and tour packages (safe to run multiple times).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final SystemSettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final DestinationRepository destinationRepository;
    private final PackageCategoryRepository categoryRepository;
    private final TourPackageRepository tourPackageRepository;
    private final HotelRepository hotelRepository;

    /**
     * Injects all required repository interfaces and the password encoder to seed data into the database.
     */
    public DataInitializer(RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           UserRepository userRepository,
                           SystemSettingsRepository settingsRepository,
                           PasswordEncoder passwordEncoder,
                           DestinationRepository destinationRepository,
                           PackageCategoryRepository categoryRepository,
                           TourPackageRepository tourPackageRepository,
                           HotelRepository hotelRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.passwordEncoder = passwordEncoder;
        this.destinationRepository = destinationRepository;
        this.categoryRepository = categoryRepository;
        this.tourPackageRepository = tourPackageRepository;
        this.hotelRepository = hotelRepository;
    }

    /**
     * Executes automatically when the Spring Boot application finishes starting up (CommandLineRunner.run).
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== TravelGO Data Initializer ===");

        // 1. Create permissions
        createPermissions();

        // 1.5. Migrate legacy TRAVEL_CONSULTANT role to PACKAGE_MANAGER if present in DB
        migrateTravelConsultantToPackageManager();

        // 2. Create roles
        Role adminRole = createRole("ADMIN", "System administrator with full access");
        Role customerRole = createRole("CUSTOMER", "Registered customer");
        Role packageManagerRole = createRole("PACKAGE_MANAGER", "Package manager staff member");
        Role visaOfficerRole = createRole("VISA_OFFICER", "Visa processing officer");

        // 3. Assign all admin permissions to ADMIN role
        assignAdminPermissions(adminRole);

        // 4. Create or update default accounts
        createAdminAccount(adminRole);
        createUserAccount("officer@travelgo.com", "Sarah Jenkins (Visa Officer)", "Officer@123", "+1 (555) 000-0002", visaOfficerRole);
        createUserAccount("customer@travelgo.com", "John Traveler", "Customer@123", "+1 (555) 000-0003", customerRole);
        createUserAccount("manager@travelgo.com", "Marcus Vance (Package Manager)", "Manager@123", "+1 (555) 000-0004", packageManagerRole);
        createUserAccount("consultant@travelgo.com", "Marcus Vance (Package Manager)", "Consultant@123", "+1 (555) 000-0004", packageManagerRole);

        // 5. Create default system settings
        createDefaultSettings();

        // 6. Seed default destinations, categories, tour packages, and hotels
        createSeedCatalog();

        System.out.println("=== Data initialization complete ===");
    }

    /**
     * Defines all granular permissions for administrative and staff operations.
     */
    private void createPermissions() {
        // Admin module permissions
        createPermission("USER_VIEW", "View users list", "USER");
        createPermission("USER_MANAGE", "Create, update, and deactivate users", "USER");
        createPermission("STAFF_VIEW", "View staff list", "STAFF");
        createPermission("STAFF_MANAGE", "Create, update, and deactivate staff", "STAFF");
        createPermission("ROLE_VIEW", "View roles", "ROLE");
        createPermission("ROLE_MANAGE", "Manage role-permission assignments", "ROLE");
        createPermission("PERMISSION_VIEW", "View permissions", "PERMISSION");
        createPermission("SYSTEM_SETTINGS_VIEW", "View system settings", "SYSTEM");
        createPermission("SYSTEM_SETTINGS_MANAGE", "Update system settings", "SYSTEM");
        createPermission("DASHBOARD_VIEW", "Access admin dashboard", "DASHBOARD");

        // Staff module permissions
        createPermission("PACKAGE_VIEW", "View tour packages", "PACKAGE");
        createPermission("PACKAGE_MANAGE", "Manage tour packages", "PACKAGE");
        createPermission("DESTINATION_VIEW", "View destinations", "DESTINATION");
        createPermission("DESTINATION_MANAGE", "Manage destinations", "DESTINATION");
        createPermission("VISA_VIEW", "View visa applications", "VISA");
        createPermission("VISA_MANAGE", "Process visa applications", "VISA");
        createPermission("BOOKING_VIEW", "View bookings", "BOOKING");
        createPermission("BOOKING_MANAGE", "Manage bookings", "BOOKING");
    }

    /**
     * Saves a new permission in the database only if it doesn't already exist (idempotent).
     */
    private void createPermission(String name, String description, String module) {
        if (!permissionRepository.existsByPermissionName(name)) {
            permissionRepository.save(new Permission(name, description, module));
            System.out.println("  Created permission: " + name);
        }
    }

    /**
     * Safely migrates existing users from the old 'TRAVEL_CONSULTANT' role to 'PACKAGE_MANAGER'.
     */
    private void migrateTravelConsultantToPackageManager() {
        Optional<Role> oldRoleOpt = roleRepository.findByRoleName("TRAVEL_CONSULTANT");
        if (oldRoleOpt.isPresent()) {
            Role oldRole = oldRoleOpt.get();
            Optional<Role> pmRoleOpt = roleRepository.findByRoleName("PACKAGE_MANAGER");
            if (pmRoleOpt.isPresent()) {
                Role pmRole = pmRoleOpt.get();
                List<User> users = userRepository.findByRoleRoleName("TRAVEL_CONSULTANT");
                for (User u : users) {
                    u.setRole(pmRole);
                    userRepository.save(u);
                }
                roleRepository.delete(oldRole);
                System.out.println("  Reassigned TRAVEL_CONSULTANT users to existing PACKAGE_MANAGER and deleted old role.");
            } else {
                oldRole.setRoleName("PACKAGE_MANAGER");
                oldRole.setDescription("Package manager staff member");
                roleRepository.save(oldRole);
                System.out.println("  Migrated TRAVEL_CONSULTANT role directly to PACKAGE_MANAGER in DB.");
            }
        }
    }

    /**
     * Finds a role by name or creates it if it does not yet exist.
     */
    private Role createRole(String roleName, String description) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role role = roleRepository.save(new Role(roleName, description));
                    System.out.println("  Created role: " + roleName);
                    return role;
                });
    }

    /**
     * Grants every system permission to the ADMIN role.
     */
    private void assignAdminPermissions(Role adminRole) {
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
        if (adminRole.getPermissions().size() != allPermissions.size()) {
            adminRole.setPermissions(allPermissions);
            roleRepository.save(adminRole);
            System.out.println("  Assigned all permissions to ADMIN role");
        }
    }

    /**
     * Convenience helper to create the primary administrator account (admin@travelgo.com).
     */
    private void createAdminAccount(Role adminRole) {
        createUserAccount("admin@travelgo.com", "System Administrator", "Admin@123", "+1 (555) 000-0001", adminRole);
    }

    /**
     * Creates or updates a user account with a BCrypt encrypted password and designated role.
     */
    private void createUserAccount(String email, String name, String rawPassword, String phone, Role role) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);
            System.out.println("  Created account: " + email + " (" + role.getRoleName() + ")");
        } else {
            System.out.println("  Synchronized account: " + email + " (" + role.getRoleName() + ")");
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
    }

    /**
     * Seeds the global company profile, contact details, business hours, and site status (ACTIVE).
     */
    private void createDefaultSettings() {
        if (settingsRepository.count() == 0) {
            SystemSettings settings = new SystemSettings();
            settings.setCompanyName("TravelGO");
            settings.setCompanyDescription("Your trusted travel partner for unforgettable journeys around the world. We offer curated tour packages, visa assistance, and personalized travel planning services.");
            settings.setCompanyEmail("info@travelgo.com");
            settings.setCompanyPhone("+1 (555) 123-4567");
            settings.setCompanyAddress("123 Travel Street, Adventure City, TC 10001");
            settings.setWebsiteUrl("http://localhost:8080");
            settings.setWebsiteStatus("ACTIVE");
            settings.setMaintenanceMessage("We are currently performing scheduled maintenance. Please check back soon.");
            settings.setCurrency("USD");
            settings.setTimezone("UTC");
            settings.setBusinessHours("Mon - Fri: 9:00 AM - 6:00 PM | Sat: 10:00 AM - 4:00 PM");
            settingsRepository.save(settings);
            System.out.println("  Created default system settings");
        } else {
            // Fix existing raw JSON businessHours if needed
            settingsRepository.findAll().forEach(s -> {
                if (s.getBusinessHours() != null && s.getBusinessHours().startsWith("{")) {
                    s.setBusinessHours("Mon - Fri: 9:00 AM - 6:00 PM | Sat: 10:00 AM - 4:00 PM");
                    settingsRepository.save(s);
                }
            });
        }
    }

    /**
     * Seeds default travel catalog items (Destinations, Categories, Tour Packages, Hotels)
     * so that the application has rich demo data for display right after startup.
     */
    private void createSeedCatalog() {
        if (tourPackageRepository.count() == 0) {
            System.out.println("  Seeding default travel catalog (Destinations, Categories, Packages, Hotels)...");

            // 1. Destinations
            Destination paris = createDestinationIfNotExists("France", "Paris",
                    "The city of lights, renowned for art, gastronomy, world-class monuments, and historical culture.",
                    "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1920&q=90");
            Destination tokyo = createDestinationIfNotExists("Japan", "Tokyo",
                    "A dynamic blend of ultra-modern neon-lit skyscrapers and historic temples with rich traditions.",
                    "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=1920&q=90");
            Destination dubai = createDestinationIfNotExists("UAE", "Dubai",
                    "A futuristic metropolis famous for luxury shopping, ultramodern architecture, and lively nightlife.",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1920&q=90");
            Destination rome = createDestinationIfNotExists("Italy", "Rome",
                    "The Eternal City, home to nearly three millennia of globally influential art, architecture, and culture.",
                    "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1920&q=90");

            // 2. Categories
            PackageCategory cultural = createCategoryIfNotExists("Cultural Heritage", "Historical tours, landmark access, and cultural immersion.");
            PackageCategory luxury = createCategoryIfNotExists("Luxury & Leisure", "Exclusive experiences, private transfers, and premier excursions.");
            PackageCategory adventure = createCategoryIfNotExists("Adventure & Safari", "Outdoor expeditions, desert safaris, and nature trails.");

            // 3. Tour Packages
            createPackage("European Classical Journey", cultural, paris,
                    "Covers Paris and Rome with guided museum access and high-speed rail transfers between cities.",
                    new BigDecimal("1299.00"), 7, 20,
                    "Return Economy Ticket Included",
                    "Skip-the-line Louvre & Colosseum, rail transfers, bilingual tour guide",
                    "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1920&q=90");

            createPackage("Tokyo & Kyoto Cultural Tour", cultural, tokyo,
                    "Discover Shinjuku, Asakusa Sensoji temple, Hakone Mt. Fuji views, and traditional Gion geisha districts.",
                    new BigDecimal("1549.00"), 8, 15,
                    "Tokyo Haneda/Narita Direct Flights Included",
                    "7-Day Shinkansen Bullet Train Pass, tea ceremony, private temple tour",
                    "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=1920&q=90");

            createPackage("Dubai City & Desert Expedition", adventure, dubai,
                    "Includes red dune safari, evening dhow cruise along Dubai Marina, and Burj Khalifa 124th floor tickets.",
                    new BigDecimal("899.00"), 5, 25,
                    "Direct Dubai Return Flights",
                    "4x4 Safari, BBQ dinner, Marina cruise, Burj Khalifa observation deck",
                    "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1920&q=90");

            createPackage("Italian Renaissance & Rome", luxury, rome,
                    "Experience ancient Roman amphitheaters, Vatican private tours, and authentic Tuscan wine tasting.",
                    new BigDecimal("1199.00"), 6, 18,
                    "Rome Fiumicino Return Flights",
                    "Vatican Museums VIP pass, Colosseum underground tour, guided wine tasting",
                    "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1920&q=90");

            // 4. Hotels (Optional Add-ons)
            createHotelIfNotExists("Grand Hotel de Paris", paris, "12 Rue de Rivoli, Paris", 4, new BigDecimal("180.00"), "Charming Parisian boutique hotel near the Louvre.");
            createHotelIfNotExists("Tokyo Skyline Shinjuku", tokyo, "3-1 Shinjuku, Tokyo", 4, new BigDecimal("150.00"), "Modern high-rise hotel offering breathtaking city views.");
            createHotelIfNotExists("Burj View Luxury Suites", dubai, "Downtown Dubai, UAE", 5, new BigDecimal("250.00"), "Opulent 5-star hotel adjacent to Dubai Mall.");
            createHotelIfNotExists("Hotel Roma Centro", rome, "Via Nazionale 44, Rome", 3, new BigDecimal("120.00"), "Comfortable central hotel within walking distance of the Colosseum.");

            System.out.println("  Seeded 4 tour packages, 4 destinations, 3 categories, and 4 optional hotels.");
        }
    }

    /**
     * Finds a Destination by city and country, or creates it if not found.
     */
    private Destination createDestinationIfNotExists(String country, String city, String description, String image) {
        List<Destination> list = destinationRepository.findAll();
        for (Destination d : list) {
            if (d.getCity().equalsIgnoreCase(city) && d.getCountry().equalsIgnoreCase(country)) {
                return d;
            }
        }
        Destination dest = new Destination();
        dest.setCountry(country);
        dest.setCity(city);
        dest.setDescription(description);
        dest.setImage(image);
        dest.setActive(true);
        return destinationRepository.save(dest);
    }

    /**
     * Finds a PackageCategory by name, or creates it if not found.
     */
    private PackageCategory createCategoryIfNotExists(String name, String description) {
        List<PackageCategory> list = categoryRepository.findAll();
        for (PackageCategory c : list) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        PackageCategory cat = new PackageCategory();
        cat.setName(name);
        cat.setDescription(description);
        cat.setActive(true);
        return categoryRepository.save(cat);
    }

    /**
     * Creates and saves a TourPackage entity with all pricing, capacity, and itinerary details.
     */
    private void createPackage(String name, PackageCategory category, Destination destination,
                               String description, BigDecimal price, int days, int capacity,
                               String flight, String services, String image) {
        TourPackage pkg = new TourPackage();
        pkg.setName(name);
        pkg.setCategory(category);
        pkg.setDestination(destination);
        pkg.setDescription(description);
        pkg.setBasePrice(price);
        pkg.setDurationDays(days);
        pkg.setMaxCapacity(capacity);
        pkg.setFlightDetails(flight);
        pkg.setIncludedServices(services);
        pkg.setImage(image);
        pkg.setActive(true);
        tourPackageRepository.save(pkg);
    }

    /**
     * Creates and saves an optional Hotel add-on entity linked to a Destination.
     */
    private void createHotelIfNotExists(String name, Destination destination, String address, int stars, BigDecimal price, String description) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setDestination(destination);
        hotel.setAddress(address);
        hotel.setStarRating(stars);
        hotel.setPricePerNight(price);
        hotel.setDescription(description);
        hotel.setActive(true);
        hotelRepository.save(hotel);
    }
}
