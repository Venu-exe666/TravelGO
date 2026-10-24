<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.7-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4.0.7"/>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/SQL%20Server-2019+-CC2927?style=for-the-badge&logo=microsoftsqlserver&logoColor=white" alt="SQL Server"/>
  <img src="https://img.shields.io/badge/Thymeleaf-Server--Side-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white" alt="Thymeleaf"/>
  <img src="https://img.shields.io/badge/License-Academic-blue?style=for-the-badge" alt="Academic License"/>
</p>

<h1 align="center">✈️ TravelGO — Smart Travel Agency Management System</h1>

<p align="center">
  A full-stack, role-based travel agency web application built with <strong>Spring Boot 4</strong>, <strong>Thymeleaf</strong>, and <strong>Microsoft SQL Server</strong>.<br/>
  Manages the complete customer journey — from browsing tour packages and booking trips, through visa processing and payment handling, to PDF ticket issuance and administrative reporting.
</p>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [System Architecture](#-system-architecture)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Database Setup](#-database-setup)
- [SMTP Email Configuration](#-smtp-email-configuration)
- [Local Setup & Run](#-local-setup--run)
- [Application Roles](#-application-roles)
- [Default Demo Accounts](#-default-demo-accounts)
- [Testing](#-testing)
- [Screenshots](#-screenshots)
- [Security Notes](#-security-notes)
- [Academic Context](#-academic-context)

---

## 🌍 Overview

**TravelGO** is a university-level integrated software project that simulates a production-grade travel agency management system. It supports four distinct user roles — **Admin**, **Customer**, **Package Manager**, and **Visa Officer** — each with dedicated dashboards, workflows, and permission-controlled access.

The application implements a realistic end-to-end travel booking lifecycle:

1. Customers browse destinations and curated tour packages on a public storefront.
2. Authenticated customers book packages, add hotel accommodations, and register traveler details.
3. Visa Officers process visa applications through an 8-stage workflow with document uploads, verification, and fee invoicing.
4. Payment processing tracks visa documentation fees and full package payments with deadline enforcement.
5. Upon full payment, Visa Officers release PDF e-tickets via email.
6. Administrators manage users, staff, roles, permissions, system settings, maintenance mode, and generate PDF reports.

---

## ✨ Key Features

### 🔓 Public Storefront
- Responsive landing page with featured destinations and tour packages
- Browse & search destinations by country or city
- Filter tour packages by keyword, destination, and category
- Detailed package pages with pricing, duration, flight details, and included services
- Visa information page, FAQ, About, and Contact form
- Contact form submissions stored in the database and linked to authenticated users

### 👤 Customer Portal
- Self-service registration with Bean Validation and BCrypt password hashing
- Personal dashboard with booking overview and statistics
- Book tour packages with travel date selection, traveler count, and optional hotel add-ons
- Multi-step booking confirmation with itemized cost breakdown
- Visa application submission with document file uploads (passport, photo, etc.)
- Real-time visa status tracking through an 8-stage workflow
- Payment checkout for visa documentation fees and full package payments
- In-app notification centre (booking confirmations, visa updates, payment receipts)
- Profile management (name, phone, address updates)
- Review and rate completed bookings (1–5 star ratings with comments)

### 📦 Package Manager (Staff)
- Dedicated Package Manager dashboard with booking and package statistics
- Full CRUD management for tour packages (name, category, destination, pricing, capacity, flight info, images)
- Destination management (country, city, description, images, active/inactive toggle)
- Hotel management (name, address, star rating, nightly price, linked to destinations)
- Package category management (Cultural Heritage, Luxury & Leisure, Adventure & Safari, etc.)
- View and manage customer bookings assigned to managed packages
- Respond to customer contact inquiries with branded HTML email replies via SMTP

### 🛂 Visa Officer (Staff)
- Dedicated Visa Officer dashboard with visa application and payment statistics
- Process visa applications through the complete lifecycle:
  `PENDING_DOCUMENTS → DOCUMENTS_SUBMITTED → DOCUMENTS_VERIFIED → PAYMENT_PENDING → PROCESSING → APPROVED / REJECTED`
- Review uploaded visa documents and add officer remarks
- Set visa charges (embassy fee + documentation fee) and generate payment invoices with deadlines
- Verify customer payments and update payment status
- Release PDF e-tickets to customers upon full payment (emailed with download link)
- Track visa status history with timestamped audit trail

### 🛡️ Admin Panel
- Centralised admin dashboard with system-wide KPIs (total users, bookings, revenue, visa stats)
- User management — view, search, and inspect all registered accounts
- Staff management — create, edit, and deactivate staff accounts with role assignment
- Role & permission management — granular RBAC with 18 permissions across 8 modules
- System settings — company profile, contact info, business hours, currency, timezone, and logo
- Maintenance mode toggle — redirects all public users to a maintenance page while admins retain access
- Downloadable PDF reports (Booking Summary, Payment & Revenue, Visa Applications, User & Staff, Tour Packages)

### 📧 Email System
- Branded HTML email templates rendered via Thymeleaf (with plain-text fallback)
- Inquiry reply emails sent to customers from staff
- Ticket release emails with secure PDF download links
- Gmail SMTP integration with TLS encryption

### 📄 PDF Generation
- Professional branded PDF reports with styled headers, data tables, and summary statistics (iText 7)
- Customer e-ticket PDF vouchers with booking details, traveler manifest, hotel info, and check-in instructions

---

## 🛠 Technology Stack

| Layer               | Technology                                                       |
|---------------------|------------------------------------------------------------------|
| **Language**        | Java 17                                                          |
| **Framework**       | Spring Boot 4.0.7                                                |
| **View Engine**     | Thymeleaf + Thymeleaf Extras (Spring Security integration)       |
| **Security**        | Spring Security 6 — session-based auth, BCrypt, role-based RBAC  |
| **Database**        | Microsoft SQL Server (via `mssql-jdbc` driver)                   |
| **ORM**             | Spring Data JPA / Hibernate (auto DDL with `update` strategy)    |
| **Validation**      | Spring Boot Starter Validation (Jakarta Bean Validation)         |
| **Email**           | Spring Boot Starter Mail (JavaMailSender, MIME, Gmail SMTP)      |
| **PDF Generation**  | iText 7 (kernel + layout modules, v7.2.6)                       |
| **Build Tool**      | Apache Maven (with Maven Wrapper `mvnw` / `mvnw.cmd`)           |
| **Dev Tools**       | Spring Boot DevTools (hot-reload in development)                 |
| **Frontend**        | HTML5, CSS3, JavaScript (server-rendered via Thymeleaf)          |

---

## 🏗 System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Browser (Client)                         │
│          Public Pages · Customer · Staff · Admin                │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP (port 8080)
┌──────────────────────────▼──────────────────────────────────────┐
│                   Spring Boot Application                       │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │  Security    │  │ Maintenance  │  │  Web Config        │    │
│  │  Filter      │  │ Interceptor  │  │  (upload paths,    │    │
│  │  Chain       │  │              │  │   static resources)│    │
│  └──────┬───────┘  └──────┬───────┘  └────────────────────┘    │
│         │                 │                                     │
│  ┌──────▼─────────────────▼──────────────────────────────────┐  │
│  │              Controllers (25 classes)                      │  │
│  │  Public · Auth · Customer · Staff · Admin · Reports       │  │
│  └──────────────────────┬────────────────────────────────────┘  │
│                         │                                       │
│  ┌──────────────────────▼────────────────────────────────────┐  │
│  │              Services (23 classes)                         │  │
│  │  Booking · Payment · Visa · Email · Report · User · ...   │  │
│  └──────────────────────┬────────────────────────────────────┘  │
│                         │                                       │
│  ┌──────────────────────▼────────────────────────────────────┐  │
│  │           Repositories (19 JPA interfaces)                │  │
│  └──────────────────────┬────────────────────────────────────┘  │
│                         │ Spring Data JPA / Hibernate           │
└─────────────────────────┼───────────────────────────────────────┘
                          │
               ┌──────────▼──────────┐
               │  Microsoft SQL      │
               │  Server (TravelGoDB)│
               └─────────────────────┘
```

---

## 📁 Project Structure

```
TravelGO/
├── pom.xml                                    # Maven build configuration
├── mvnw / mvnw.cmd                            # Maven Wrapper scripts
├── .gitignore                                 # Git exclusions (secrets, IDE, uploads)
├── application-example.properties             # Safe template for local configuration
├── uploads/                                   # User-uploaded visa documents (gitignored)
│
└── src/main/
    ├── java/com/travelgo/
    │   ├── TravelGoApplication.java           # Spring Boot entry point
    │   ├── config/
    │   │   ├── SecurityConfig.java            # Spring Security (RBAC, login, BCrypt)
    │   │   ├── DataInitializer.java           # Seed data (roles, permissions, demo accounts, packages)
    │   │   ├── MaintenanceInterceptor.java    # Site-wide maintenance mode gatekeeper
    │   │   ├── WebConfig.java                 # Static resource & upload path mapping
    │   │   └── BookingStatusConstraintMigration.java
    │   ├── controller/                        # 25 MVC controllers
    │   │   ├── PublicController.java          # Home, destinations, packages, contact, FAQ
    │   │   ├── AuthController.java            # Login & registration
    │   │   ├── Customer*Controller.java       # Booking, payment, visa, profile, reviews, notifications
    │   │   ├── PackageManager*Controller.java # Packages, destinations, hotels, categories
    │   │   ├── Staff*Controller.java          # Visa processing, payments, bookings, inquiries
    │   │   ├── Admin*Controller.java          # Dashboard, users, staff, roles, settings
    │   │   ├── ReportController.java          # PDF report downloads
    │   │   └── SystemSettingsController.java  # Admin system configuration
    │   ├── entity/                            # 19 JPA entity classes
    │   │   ├── User.java                      # Unified user table (all roles)
    │   │   ├── Role.java / Permission.java    # RBAC model (many-to-many)
    │   │   ├── Booking.java                   # Tour reservations
    │   │   ├── TourPackage.java               # Curated travel packages
    │   │   ├── Destination.java / Hotel.java  # Travel catalog
    │   │   ├── VisaApplication.java           # Visa lifecycle tracking
    │   │   ├── VisaDocument.java              # Uploaded passport/photo files
    │   │   ├── Payment.java / Refund.java     # Financial transactions
    │   │   ├── Review.java                    # Customer ratings & feedback
    │   │   ├── Notification.java              # In-app alert messages
    │   │   ├── ContactMessage.java            # Public inquiry submissions
    │   │   └── SystemSettings.java            # Singleton site configuration
    │   ├── dto/                               # Data transfer objects
    │   ├── enums/                             # Status enums (Booking, Visa, Payment, Refund)
    │   ├── repository/                        # 19 Spring Data JPA repositories
    │   ├── service/                           # 23 business logic services
    │   │   ├── BookingService.java            # Booking lifecycle & validation
    │   │   ├── PaymentService.java            # Payment processing & deadlines
    │   │   ├── VisaApplicationService.java    # 8-stage visa workflow engine
    │   │   ├── EmailService.java              # Branded HTML email dispatch
    │   │   ├── ReportService.java             # iText PDF report generation
    │   │   ├── BookingTicketPdfService.java   # Customer e-ticket PDF creation
    │   │   └── ...                            # User, role, notification, settings services
    │   └── exception/
    │       └── GlobalExceptionHandler.java    # Centralised error handling
    │
    └── resources/
        ├── application-example.properties     # Configuration template (safe placeholders)
        ├── static/
        │   ├── css/                           # Stylesheets
        │   ├── js/                            # Client-side JavaScript
        │   ├── images/                        # Static images
        │   └── videos/                        # Background videos
        └── templates/                         # Thymeleaf HTML templates
            ├── index.html                     # Public landing page
            ├── packages.html                  # Tour package catalog
            ├── destinations.html              # Destinations listing
            ├── visa-info.html / faq.html      # Informational pages
            ├── contact.html / about.html      # Contact form & about page
            ├── maintenance.html / error.html  # System status pages
            ├── auth/                          # Login & registration forms
            ├── customer/                      # Customer dashboard, bookings, payments, visas
            ├── staff/                         # Staff dashboards, visa processing, package management
            ├── admin/                         # Admin dashboard, users, staff, roles, settings, reports
            ├── mail/                          # HTML email templates (inquiry reply)
            └── fragments/                     # Reusable navbar, footer, sidebar components
```

---

## ✅ Prerequisites

| Requirement             | Version / Notes                                                    |
|-------------------------|--------------------------------------------------------------------|
| **Java JDK**            | 17 or higher                                                      |
| **Microsoft SQL Server**| 2019 or later (Express edition is sufficient)                      |
| **Maven**               | 3.9+ (or use the included Maven Wrapper `mvnw` / `mvnw.cmd`)      |
| **Git**                 | For cloning the repository                                         |
| **SMTP Account**        | Gmail with App Password (or any SMTP provider) for email features  |

---

## 🗄 Database Setup

1. **Create the database** in SQL Server Management Studio (SSMS) or via command line:

   ```sql
   CREATE DATABASE TravelGoDB;
   ```

2. **Create the local configuration file** — copy the example template and fill in your credentials:

   ```bash
   cp src/main/resources/application-example.properties local.properties
   ```

3. **Edit `local.properties`** in the project root with your actual values:

   ```properties
   # ---- Database (SQL Server) ----
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TravelGoDB;encrypt=false;trustServerCertificate=true
   spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
   spring.datasource.username=YOUR_DB_USERNAME
   spring.datasource.password=YOUR_DB_PASSWORD

   # ---- JPA / Hibernate ----
   spring.jpa.database-platform=org.hibernate.dialect.SQLServerDialect
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false
   spring.jpa.open-in-view=true

   # ---- Server ----
   server.port=8080
   server.address=127.0.0.1

   # ---- Thymeleaf ----
   spring.thymeleaf.cache=false

   # ---- File Uploads ----
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB
   ```

> **Note:** `local.properties` is listed in `.gitignore` and will never be committed. Hibernate's `ddl-auto=update` strategy auto-creates all required tables and columns on first startup.

---

## 📧 SMTP Email Configuration

Add the following to your `local.properties` to enable email functionality:

```properties
# ---- Email (Gmail SMTP) ----
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> **Gmail App Password:** Go to [Google Account → Security → 2-Step Verification → App Passwords](https://myaccount.google.com/apppasswords) to generate a 16-character app-specific password. Do **not** use your regular Gmail password.

---

## 🚀 Local Setup & Run

```bash
# 1. Clone the repository
git clone https://github.com/Venu-exe666/TravelGO.git
cd TravelGO

# 2. Create your local configuration (see Database Setup above)
cp src/main/resources/application-example.properties local.properties
# Edit local.properties with your database and SMTP credentials

# 3. Build the project
./mvnw clean install -DskipTests

# 4. Run the application
./mvnw spring-boot:run
```

**On Windows (PowerShell):**
```powershell
.\mvnw.cmd clean install -DskipTests
.\mvnw.cmd spring-boot:run
```

Once started, open your browser and navigate to:

```
http://localhost:8080
```

> On first startup, the `DataInitializer` automatically seeds:
> - 4 roles (Admin, Customer, Package Manager, Visa Officer)
> - 18 granular permissions across 8 modules
> - 5 demo user accounts
> - 4 tour packages with destinations, categories, and hotels

---

## 👥 Application Roles

| Role                | Access Level                 | Key Capabilities                                                                                     |
|---------------------|------------------------------|------------------------------------------------------------------------------------------------------|
| **ADMIN**           | Full system access           | User/staff management, role & permission control, system settings, maintenance mode, PDF reports      |
| **CUSTOMER**        | Customer portal              | Browse packages, book tours, submit visa applications, make payments, rate experiences, notifications |
| **PACKAGE_MANAGER** | Staff — Package operations   | Manage packages/destinations/hotels/categories, handle bookings, reply to customer inquiries          |
| **VISA_OFFICER**    | Staff — Visa & payments      | Process visa applications, verify documents, manage payments, release e-tickets, visa dashboard       |

---

## 🔑 Default Demo Accounts

The following accounts are automatically created on first startup by `DataInitializer`:

| Role             | Email                      | Password        |
|------------------|----------------------------|-----------------|
| Admin            | `admin@travelgo.com`       | `Admin@123`     |
| Customer         | `customer@travelgo.com`    | `Customer@123`  |
| Package Manager  | `manager@travelgo.com`     | `Manager@123`   |
| Visa Officer     | `officer@travelgo.com`     | `Officer@123`   |

> ⚠️ **These are demo-only credentials.** Change all default passwords immediately in any non-development deployment. See [Security Notes](#-security-notes).

---

## 🧪 Testing

The project includes test dependencies for comprehensive testing:

```xml
<!-- Test dependencies in pom.xml -->
spring-boot-starter-data-jpa-test
spring-boot-starter-security-test
spring-boot-starter-thymeleaf-test
spring-boot-starter-validation-test
spring-boot-starter-webmvc-test
```

**Run all tests:**

```bash
./mvnw test
```

**Run with verbose output:**

```bash
./mvnw test -Dspring-boot.test.verbose=true
```

---

## 📸 Screenshots

> Screenshots can be added to a `docs/screenshots/` directory and linked below.

| Screen                         | Description                                                  |
|--------------------------------|--------------------------------------------------------------|
| 🏠 **Landing Page**           | <!-- ![Landing](docs/screenshots/landing.png) -->            |
| 📦 **Package Catalog**        | <!-- ![Packages](docs/screenshots/packages.png) -->          |
| 📝 **Booking Form**           | <!-- ![Booking](docs/screenshots/booking-form.png) -->       |
| 💳 **Payment Checkout**       | <!-- ![Payment](docs/screenshots/payment.png) -->            |
| 🛂 **Visa Application**       | <!-- ![Visa](docs/screenshots/visa-application.png) -->      |
| 📊 **Admin Dashboard**        | <!-- ![Admin](docs/screenshots/admin-dashboard.png) -->      |
| 👥 **Staff Dashboard**        | <!-- ![Staff](docs/screenshots/staff-dashboard.png) -->      |
| 📄 **PDF Report Sample**      | <!-- ![Report](docs/screenshots/pdf-report.png) -->          |

*To add screenshots: capture each screen, save to `docs/screenshots/`, and uncomment the image links above.*

---

## 🔒 Security Notes

| Concern                      | Implementation                                                                            |
|------------------------------|-------------------------------------------------------------------------------------------|
| **Password Storage**         | BCrypt hashing (strength 10) — passwords are never stored in plain text                   |
| **Authentication**           | Session-based form login via Spring Security with CSRF protection enabled by default       |
| **Authorization**            | Role-based access control (RBAC) with 18 granular permissions across 8 modules             |
| **Route Protection**         | URL-level security rules in `SecurityConfig` — admin, staff, and customer routes guarded   |
| **Secrets Management**       | `local.properties` file (gitignored) — database and SMTP credentials never committed       |
| **File Uploads**             | `uploads/` directory gitignored — user documents not committed to version control          |
| **Demo Accounts**            | Seeded by `DataInitializer` for development/demo only — **change passwords in production** |
| **Maintenance Mode**         | Admin-controlled site lockdown — public users redirected; admin access preserved            |

> **⚠️ Important:** The default demo account passwords (`Admin@123`, etc.) are intended **only for local development and academic demonstration**. Never deploy this application to a public server without changing all default credentials, enabling HTTPS, and reviewing the security configuration.

---

## 🎓 Academic Context

**TravelGO** was developed as a university Integrated Software Project Management (ISPM) coursework submission. It demonstrates the practical application of:

- **Full-stack Java web development** with Spring Boot and Thymeleaf
- **Role-based access control (RBAC)** with granular permission management
- **Relational database design** with JPA entity relationships (One-to-One, One-to-Many, Many-to-Many)
- **Multi-stage business workflows** (booking lifecycle, visa processing pipeline)
- **Server-side email integration** with branded HTML templates
- **PDF document generation** for business reports and customer documents
- **Software engineering practices** — MVC architecture, service layer separation, DTO pattern, centralised exception handling, data seeding, and Git version control

---

<p align="center">
  Made with ☕ and Spring Boot<br/>
  <strong>TravelGO</strong> — Your Trusted Travel Partner
</p>
