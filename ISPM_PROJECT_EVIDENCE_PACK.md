# ISPM Project Evidence Pack

**Project audited:** TravelGO — Smart Travel Agency Management System  
**Audit date:** 21 October 2026  
**Workspace:** `G:\a\ISE3.0`  
**Purpose:** Evidence source for a later ISPM final report; this document is not the final university report.

### Evidence conventions

- **VERIFIED** means the statement is directly supported by repository source, configuration, generated artifacts, or test output.
- **INFERRED** means the interpretation is reasonable but is not explicitly documented or independently demonstrated.
- **NOT VERIFIED** means student/team confirmation or evidence outside this workspace is required.
- Feature status uses the requested classifications. “Implemented and verified” is reserved for behavior covered by the stored automated-test evidence; the presence of source code and UI alone is classified as “implemented but not functionally verified.”
- No password, token, mail credential, or complete secret value is reproduced here. The repository contains local/seeding credential concerns, which are described without disclosing their values.

## 1. Project Overview

### Identity and system type

**VERIFIED:** The official build name is **TravelGo**, and the Maven description is **“TravelGO - Smart Travel Agency Management System (University Project)”** (`pom.xml`). The configured application name is TravelGO (`src/main/resources/application.properties`). It is a Java/Spring Boot, server-rendered travel-agency management web application combining a public catalogue, customer self-service, operational staff workflows, visa processing, payment records, administration, and PDF reporting.

Only one application is present. The project is a modular monolith: one Maven artifact, one Spring application, one relational database connection, server-side Thymeleaf views, and static browser assets. There is no separately deployed SPA, mobile application, or REST service in the workspace.

### Background, problem, motivation, and proposed solution

**INFERRED from implemented workflows:** Travel agencies must coordinate package discovery, bookings, travelers, optional hotels, visa evidence, fee collection, approvals, ticket release, customer inquiries, staff access, and operational reporting. Fragmented manual handling creates duplicated records, weak status visibility, and opportunities to advance a booking before prerequisite visa or payment steps are complete.

**VERIFIED solution evidence:** TravelGO centralizes those records and applies workflow rules in services. Examples include customer ownership checks (`CustomerBookingController`, `CustomerVisaController`, `CustomerPaymentController`), visa approval as a prerequisite for full-package payment (`PaymentService.processPayment`), verified full payment as a prerequisite for ticket release (`BookingService.issueTickets`), role-specific URL access (`SecurityConfig`), and operational reports (`ReportController`, `ReportService`).

**NOT VERIFIED:** No approved business case, client brief, formal problem statement, benefits-management plan, market study, or requirements specification is present. The preceding problem statement is therefore an evidence-based interpretation, not a claim about the team's original approved rationale.

### Objectives and practical importance

The implementation supports the following **inferred objectives**:

1. Provide public discovery of destinations, packages, visa information, FAQs, and contact options.
2. Allow customers to register, authenticate, maintain profiles, create and modify bookings, add travelers/hotels, apply for visas, upload supporting documents, record payments, receive notifications, download tickets, and submit reviews.
3. Allow package-management staff to maintain destinations, categories, hotels, packages, bookings, and inquiries.
4. Allow visa officers to verify documents, set charges, progress or reject applications, review payments, and release tickets.
5. Allow administrators to manage users, staff, roles, permissions, settings, maintenance mode, dashboards, and PDF reports.
6. Preserve core workflow integrity with state checks and relational constraints.

The system's practical importance is that it puts the customer and staff views over a shared data model and encodes dependencies between booking, visa, payment, and ticket stages. This can reduce manual reconciliation and improve traceability. That value is **potential**, because no production usage, time saving, error reduction, adoption, or stakeholder satisfaction measurement is available.

### Scope and system boundary

**Included and implemented in source:** public catalogue and information pages; registration and session login; customer profiles; bookings, travelers, optional hotel selection, visas and documents; payment records and refunds; notifications and email; reviews; staff catalog/booking/inquiry operations; admin account/RBAC/settings functions; maintenance mode; dashboards; and PDF exports.

**Outside or not evidenced:** real payment authorization or settlement; embassy/consulate API submission; airline/hotel inventory or reservation APIs; live ticketing/GDS integration; accounting integration; SMS/push notifications; a production cloud environment; data migration from a legacy system; native mobile clients; analytics telemetry; and formal PM tooling.

### Target users, roles, and stakeholders

| Party | Evidence-based role | Status |
|---|---|---|
| Public visitor | Browse catalogue/information and send inquiries | **VERIFIED** by `PublicController` and public templates |
| Customer | Register; manage own bookings, visa files, payments, profile, notifications, tickets, and reviews | **VERIFIED** by customer controllers and `SecurityConfig` |
| Package Manager | Maintain catalogue and bookings; access shared staff functions | **VERIFIED** by `/staff/...` controllers and security matchers |
| Visa Officer | Verify visa documents, manage charges/payments/statuses, and release tickets | **VERIFIED** by `StaffVisaController`, `StaffPaymentController`, and security matchers |
| Travel Consultant | Legacy/compatibility role allowed on several staff routes, while startup code migrates it to Package Manager | **VERIFIED**, but role intent is inconsistent (`SecurityConfig`, `DataInitializer.migrateTravelConsultantToPackageManager`) |
| Administrator | Manage accounts, roles/permission assignments, settings, dashboards, and reports | **VERIFIED** by admin controllers and `/admin/**` rule |
| Project team, lecturer, client, sponsor, external agencies | Likely project stakeholders | **NOT VERIFIED**; no stakeholder register or team documentation exists |

### Main deliverables and success criteria

**VERIFIED deliverable artifacts:** 102 main Java files (approximately 7,568 lines), 50 HTML templates (approximately 14,756 lines), static CSS/JavaScript/media, 19 JPA entities, 19 repositories, 23 services, 25 controllers, five test classes, a packaged JAR, stored test reports, two PlantUML admin sequence sources, an HTML diagram viewer, and a run script.

**NOT VERIFIED:** There is no baselined deliverable list or agreed acceptance criteria. A defensible technical success criterion would be correct role access plus valid end-to-end booking→visa→payment→ticket workflow, but the repository does not document client acceptance of this criterion and tests cover only part of it.

### Assumptions and constraints

- **VERIFIED:** Java 17 is the Maven target; the run helper accepts Java 17+ or 21 (`pom.xml`, `run.ps1`).
- **VERIFIED:** The primary local runtime assumes SQL Server on localhost and binds the web server to `127.0.0.1:8080` (`application.properties`).
- **VERIFIED:** Hibernate schema mode is `update`, so the application can mutate schema at startup. An additional startup migration alters the booking-status constraint (`BookingStatusConstraintMigration`).
- **VERIFIED:** Uploaded files use local relative storage, so horizontal scaling or ephemeral hosting would require redesign (`CustomerVisaController`, `WebConfig`).
- **VERIFIED:** SMTP must be configured for email; failures are handled as best effort in some flows (`EmailService`, `ContactMessageService`, `StaffVisaController`).
- **INFERRED:** The application assumes one database and trusted operational staff. No multi-tenancy is modeled.
- **NOT VERIFIED:** Expected user volume, availability target, supported browsers, accessibility level, retention rules, budget, and legal/privacy requirements.

## 2. Technical Architecture and Technology Stack

### Technology stack

| Area | Technology | Evidence and qualification |
|---|---|---|
| Language/runtime | Java 17 | `pom.xml`; run helper searches for Java 17+ |
| Application framework | Spring Boot 4.0.7 | parent version in `pom.xml` |
| Web layer | Spring Web MVC, Jakarta Servlet | dependency and controller annotations |
| View layer | Thymeleaf plus Thymeleaf Spring Security extras | `pom.xml`, 50 templates |
| Browser layer | HTML, CSS, vanilla JavaScript, SVG, Google Fonts | `src/main/resources/templates`, `static/css`, `static/js` |
| Persistence | Spring Data JPA/Hibernate | `pom.xml`, entities and repositories |
| Primary database | Microsoft SQL Server | JDBC driver and active application properties |
| Alternate configuration | MySQL properties file | `application-mysql.properties`; **not currently runnable as configured** because `pom.xml` has no MySQL driver |
| Authentication | Spring Security form login, DAO user details, session cookie | `SecurityConfig`, `CustomUserDetailsService` |
| Password storage | BCrypt strength 10 | `SecurityConfig.passwordEncoder` |
| Validation | Jakarta Bean Validation plus controller checks | DTO annotations and controllers |
| Email | Spring Mail/JavaMailSender over configured SMTP | `EmailService`, mail templates |
| Documents/reports | iText 7 PDF generation | iText dependencies, `ReportService`, `BookingTicketPdfService` |
| Build | Maven Wrapper and Spring Boot Maven plugin | `mvnw`, `mvnw.cmd`, `pom.xml` |
| Testing | JUnit 5, Mockito, Spring Boot Test, MockMvc, Spring Security Test, Surefire | test sources and `target/surefire-reports` |
| Version control | Git conventions only | `.gitignore`/`.gitattributes` exist; **no `.git` metadata is available** |
| Deployment | Local executable JAR/run script | `target/*.jar`, `run.ps1`; no container/cloud/IaC evidence |

The walkthrough claims H2 and MySQL readiness, but the current POM includes SQL Server only. This is a documented/implementation discrepancy (`src/main/resources/templates/walkthrough.md` versus `pom.xml` and active properties).

### Architecture

```text
Browser
  -> Spring Security filter chain / session authentication
  -> MaintenanceInterceptor
  -> Spring MVC Controller
  -> Service (workflow and transaction logic)
  -> Spring Data JPA Repository
  -> SQL Server

Controller -> Thymeleaf template -> HTML response
Service -> JavaMailSender/SMTP (email)
Service -> iText (PDF stream)
Controller -> local uploads directory (visa files)
```

This is a layered, server-rendered MVC architecture. Browser forms submit conventional GET/POST requests. Controllers prepare models and redirects; services contain most workflow rules; repositories provide JPA access; entities map the database. No JSON REST API, API versioning, message broker, or independent frontend/backend deployment was found.

### Authentication and authorization flow

1. A user submits email and password to `/auth/login`.
2. Spring Security calls `CustomUserDetailsService`, which loads the database user.
3. BCrypt verifies the password; the active flag is passed into Spring Security account state.
4. A server-side authenticated session is established.
5. `customSuccessHandler` redirects by the first role authority.
6. URL patterns enforce CUSTOMER, ADMIN, PACKAGE_MANAGER/TRAVEL_CONSULTANT, or VISA_OFFICER access.
7. Customer controllers additionally compare the authenticated database user ID with the booking/visa owner.

**Important limitation:** permissions can be created and assigned to roles, but searches found no `hasAuthority`, permission-based method security, or conversion of permission entities into granted authorities. Runtime authorization is role-based; the granular permission UI/data model is administrative metadata rather than enforced fine-grained RBAC.

### Data flow example

For the principal business flow, a customer selects an active package and future date; server code copies the unit price and calculates total price, creates a pending booking and optional hotel record, then creates a visa application. Documents are stored locally and referenced in the database. A visa officer verifies files and sets charges. A visa/documentation payment advances processing; approval opens the package-payment period; a full-package payment moves the booking to `PAID_AWAITING_TICKETS`; officer release makes it `CONFIRMED` and can send an authenticated ticket link. These transitions are distributed across `CustomerBookingController`, `VisaApplicationService`, `PaymentService`, `BookingService`, and `StaffVisaController`.

### External connections

- SMTP email is a real technical integration point, although successful delivery was not verified.
- Google Fonts are loaded by templates from an external web origin.
- There is **no actual payment processor** despite card/wallet/PayPal-looking UI.
- There is **no embassy, airline, hotel inventory, maps, identity, or analytics API** in source.

### Security analysis

**Controls evidenced:** BCrypt; session invalidation and JSESSIONID deletion on logout; route-level role rules; default Spring Security CSRF protection (not disabled); DTO validation; database uniqueness/nullability; transaction annotations; customer ownership checks on sensitive customer records; file size and extension checks; `rel="noopener"` on some document links; authenticated ticket endpoint with ownership check.

**Material limitations and risks:**

1. **Critical—document confidentiality:** `/uploads/**` is explicitly `permitAll` and mapped directly to the filesystem. Visa documents can be fetched without authentication if a URL is known (`SecurityConfig`, `WebConfig`). UUID-prefixed names reduce guessing but are not authorization.
2. **Critical—simulated/trusted payments:** customer payment requests accept client-supplied `paymentType` and `amount`; `processPayment` marks them paid without gateway confirmation or server-side amount recalculation. Card/CVC fields are visual inputs that are not processed by a gateway. This is demonstrator behavior, not production payment processing.
3. **High—embedded demonstration credentials:** startup/template material includes default account credentials and a prefilled login form. Values are intentionally omitted here. Defaults should not ship outside an isolated demo.
4. **High—file validation:** uploads check extension and size, not MIME signature, malware, quarantine, or path-normalized original filename. Files are stored under the application working directory.
5. **Medium—permission model gap:** role-permission assignments do not affect authorization decisions.
6. **Medium—weak account lifecycle:** minimum password length is six; no MFA, rate limiting, lockout policy, password reset, email verification, or session policy is documented.
7. **Medium—operational exposure:** HTTP/localhost configuration contains no TLS, secure-cookie, reverse-proxy, or security-header configuration. Production infrastructure is absent.
8. **Medium—information handling:** several controllers return raw exception messages in flash messages; console logging includes email addresses. No structured audit logging or privacy retention policy exists.
9. **Medium—data/schema operations:** `ddl-auto=update` and startup DDL/data initialization are convenient for development but unsuitable as controlled production migrations.
10. **Low/medium—expiry automation:** deadline fields and `EXPIRED` enum values exist, but no scheduler marks overdue bookings/visas automatically.

## 3. Functional Requirements and System Modules

### Feature inventory

| Module | Purpose and major features | Roles | Key evidence | Status | Dependencies and limitations |
|---|---|---|---|---|---|
| Public website | Home, destination/package search, visa info, about, contact, FAQ, maintenance | Public | `PublicController`; public templates | Implemented but not functionally verified | No SEO/accessibility/performance evidence; external fonts |
| Registration/login | Customer registration, form login/logout, role redirect, active-account check | Public/all roles | `AuthController`, `UserService`, `SecurityConfig`, DTOs | Implemented but not functionally verified | Default demo credentials; no reset/MFA/verification |
| Customer profile | View/update authenticated customer's name, phone, address | Customer | `CustomerProfileController`; three passing tests | **Implemented and verified** for authenticated record selection, submitted-ID avoidance, and invalid phone rejection | No broader UI/e2e or concurrency test |
| Package catalogue | Maintain/search active destinations, categories, packages, hotels | Public, Package Manager, Travel Consultant | four package-manager controllers/services/repositories | Implemented but not functionally verified | No inventory supplier integration; deletion behavior not fully tested |
| Booking | Create, list, view, modify, cancel, approve; price snapshot; deadlines | Customer, package staff | `CustomerBookingController`, `StaffBookingController`, `BookingService` | Partially verified | Ticket release rule tested; create/modify/cancel/approval flows not tested; no capacity concurrency control |
| Travelers/hotels | Add traveler identity and one optional hotel reservation per booking | Customer | `Traveler`, `BookingHotel`, controller/service logic | Implemented but not functionally verified | No passport uniqueness/format validation; hotel is a local record, not an external reservation |
| Visa application | Apply, upload documents, verify/request more, set charges, process, approve/reject | Customer, Visa Officer | visa controllers/services/entities | Implemented but not functionally verified | Public file URLs; no embassy integration; weak file inspection; status-history table not populated |
| Payment records | Visa/full-package payment records; manual staff recording/status update; totals | Customer, Visa Officer | `PaymentService`, payment controllers/templates; two passing tests | **Partially implemented** and partially verified | No gateway or settlement; trusts client amount; “wallets” are presentation only |
| Ticket release/PDF | Gate release on paid full package; customer-owned PDF download; email/in-app notice | Visa Officer, Customer | `BookingService.issueTickets`, `BookingTicketPdfService`, controller; one passing test | **Implemented and verified** for service release precondition | PDF/email delivery and unauthorized-access behavior not in stored tests |
| Refund | Create refund record for rejected visa/manual refunded status | Visa Officer | `RefundService`, `PaymentService`, `Refund` | Partially implemented | Named “simulate”; no external payout; rejection may fail if no original payment; selection uses first payment rather than explicitly correct type |
| Notifications | Persist and list customer-specific notifications | Customer/system | `NotificationService`, customer controller | Implemented but not functionally verified | No read/unread action exposed; no push service |
| Reviews | One review per booking, only owner and confirmed booking | Customer | `CustomerReviewController`, unique booking relationship | Implemented but not functionally verified | Rating 1–5 checked by UI/comment intent, but entity has no Bean Validation/DB check evidenced |
| Inquiries/email | Public message capture; staff reply; best-effort email | Public/staff | contact controller/service, mail template, `EmailService` | Implemented but not functionally verified | SMTP delivery not tested; saved as resolved even if email fails |
| User/staff admin | Search/update/toggle users; create/update staff; assign roles | Admin | management controllers/services/templates | Implemented but not functionally verified | No audit trail; default account seeding overwrites/synchronizes demo accounts at startup |
| Roles/permissions | View roles/permissions and edit role-permission links | Admin | `RoleController`, `PermissionController`, `RoleService` | Partially implemented | Permission assignments are not enforced in runtime access checks |
| Settings/maintenance | Company/contact/hours/status configuration; maintenance interceptor | Admin/public | settings controller/service, interceptor | Implemented but not functionally verified | DB lookup per request; non-admin staff are also blocked during maintenance; no cache/test evidence |
| Dashboards | Admin, customer, visa, package-management metrics/work queues | Role-specific | dashboard controllers/templates | Implemented but not comprehensively verified | Current stored dashboard suite proves only one customer workflow-page test, not all nine methods in source |
| Reports | Five admin and three staff PDF exports | Admin/staff | `ReportController`, `ReportService` | Implemented but not functionally verified | No report correctness/large-data/access-specific tests |
| Visa status history | Entity/repository/generic CRUD service | Internal | `VisaStatusHistory*` files | **Partially implemented** | No production workflow calls save history; no UI |

### Functional requirements reconstructed from code

The following are **VERIFIED as implemented rules**, not confirmed original requirements:

- The system shall expose public informational and catalogue pages.
- Customers shall register with a unique email and authenticate using email/password.
- The system shall restrict URLs by role and redirect users to role-specific dashboards.
- Customers shall only access and mutate their own booking, payment, visa, traveler, ticket, and profile context.
- Booking travel dates shall be at least four days ahead; traveler count shall be at least one and not exceed package capacity.
- The system shall snapshot package price and calculate booking/hotel totals server-side during booking creation.
- A visa application shall be unique per booking; documents shall precede verification/charges.
- Visa-documentation payment shall precede active processing; visa approval shall precede full-package payment.
- Full payment shall precede officer ticket release.
- Customer cancellation shall be limited to unpaid pending bookings.
- A confirmed booking shall be required for a review, with one review per booking.
- Admin/staff shall generate role-appropriate PDF reports.

**NOT VERIFIED:** stakeholder priority, requirement IDs, acceptance criteria, traceability, and formal approval for any of these reconstructed requirements.

### Database structure and integrity

The 19 entity-backed tables are: `users`, `roles`, `permissions`, implicit `role_permissions`, `system_settings`, `destinations`, `package_categories`, `tour_packages`, `hotels`, `bookings`, `booking_hotels`, `travelers`, `visa_applications`, `visa_documents`, `visa_status_history`, `payments`, `refunds`, `notifications`, `reviews`, and `contact_messages`.

Key relationships are User→Role (many-to-one), Role↔Permission (many-to-many), Package→Category/Destination, Hotel→Destination, Booking→User/Package, BookingHotel→Booking (one-to-one) and Hotel, Traveler→Booking, VisaApplication→Booking (one-to-one), VisaDocument/History→VisaApplication, Payment→Booking, Refund→Payment (one-to-one), Review→Booking (one-to-one) plus User/Package, Notification→User, and ContactMessage→optional sender/assignee.

**Integrity controls:** unique user email, role name, permission name, package category name, payment transaction reference, visa per booking, hotel add-on per booking, refund per payment, and review per booking; non-null columns and decimal precision; string-backed enums; entity lifecycle timestamps; transactional workflow services; and a startup SQL Server check-constraint migration for booking statuses.

**Limitations:** Hibernate `update` replaces versioned migrations; entity validation is uneven; referential delete/cascade behavior is not comprehensively declared or tested; no optimistic-lock version fields exist; capacity is checked without locking; history logging is not wired; and database indexes/performance plans are not documented.

## 4. Non-Functional Requirements

No formal NFR specification exists. The following are reconstructed evidence and gaps.

| NFR area | Evidence | Assessment |
|---|---|---|
| Security | BCrypt, Spring Security, role routes, ownership checks, CSRF default | Partially addressed; serious upload/payment/default-credential risks remain |
| Usability | Responsive templates, consistent navigation, validation feedback, themed dashboards | Implemented presentation effort; no user/usability/accessibility study |
| Reliability | Transactional core services, state preconditions, error handler, email fallbacks | Partial; no resilience, backup, recovery, or fault-injection evidence |
| Performance | JPA queries/search and local server-rendered pages | No load test, caching plan, pagination standard, query profiling, or SLA |
| Scalability | Layered monolith | Local files, single DB, in-memory session assumptions and `findAll()` aggregates constrain scaling |
| Maintainability | Conventional controller/service/repository separation; named enums; Maven build | Good structural baseline; large templates, comments inconsistent with code, no quality/static-analysis metrics |
| Portability | Java/Maven wrapper and alternate properties | SQL Server-specific dialect/migration; MySQL profile lacks driver; local paths in run script |
| Testability | Dependency injection, unit tests, MockMvc | Only 267 test lines and eight stored executions; no coverage figure |
| Availability | Maintenance mode | No HA, health/metrics, alerting, uptime target, rollback, or incident process |
| Privacy | Owner checks on dynamic pages | Public document route and real-looking uploaded files create a confidentiality concern; no retention/consent policy |
| Accessibility | Labels/SVG/templates observed | WCAG level and automated/manual accessibility testing are **NOT VERIFIED** |
| Compatibility | Browser-based MVC | Supported browsers/devices are **NOT VERIFIED** |

## 5. Project Execution

### Development approach

**INFERRED:** The repository snapshot suggests iterative/incremental development: early admin/auth artifacts, later media/catalog work, bulk backend consolidation, subsequent diagrams, workflow/test additions, and final UI/profile refinements. This resembles an iterative or hybrid approach more than a single sequential delivery. It must not be called Scrum because no sprint goals, backlog, ceremonies, product owner, velocity, or increment acceptance evidence exists.

### Lifecycle evidence

| Stage | Evidence | Confidence |
|---|---|---|
| Initiation/problem definition | Project description only | LOW; no charter/business case |
| Planning/design | Layered structure, entities, comments, PlantUML admin flows | MEDIUM for technical design; LOW for formal planning |
| Construction | Controllers/services/entities/templates and modification timestamps | HIGH that implementation occurred |
| Integration | Booking/visa/payment dependencies, email, PDF, database startup | HIGH in source; MEDIUM functionally |
| Verification | Five test classes and Surefire outputs | HIGH for eight recorded tests; LOW for total coverage |
| Deployment/transition | local run script and JAR | MEDIUM for local packaging; LOW for deployment readiness |
| Closure | None | NOT VERIFIED |

### Workflow and task distribution

The code follows feature-oriented controller/service/repository work, but there are no task assignments, owners, branch names, pull requests, review approvals, or contributor records. File timestamps cannot establish authorship. Team roles and division of work require student confirmation.

### Integration and issue-resolution evidence

- `BookingStatusConstraintMigration` explicitly adapts a legacy SQL Server status constraint for `PAID_AWAITING_TICKETS`, indicating a schema/workflow compatibility issue and a coded response.
- `DataInitializer.migrateTravelConsultantToPackageManager` indicates a role-model change and compatibility handling.
- Test names show targeted fixes around authenticated profile ownership, invalid phone handling, payment-to-status transitions, and ticket-release gating.
- 29 small Surefire dumpstream files contain a Maven classpath/root warning. They show repeated test invocations, not necessarily defects or failed tests.
- Comments and mojibake characters in some source/templates indicate documentation/encoding quality debt.

**NOT VERIFIED:** why changes were requested, who approved them, whether defects came from testing or stakeholder feedback, and whether code review occurred.

## 6. Monitoring and Control

### Evidence present

- **Build/test monitoring:** Surefire XML/text reports record eight latest executions with 0 failures, 0 errors, and 0 skipped: three profile tests, two payment tests, one ticket test, one context test, and one customer workflow rendering test.
- **Important qualification:** `DashboardRenderingTests.java` currently contains nine methods, but its latest report records only `customerWorkflowPagesRender`. The other eight methods cannot be claimed as passing from this report set.
- **Build artifacts:** an executable JAR and original JAR were produced on 17 October 2026.
- **Technical state monitoring in the product:** dashboards display database-derived counts and payment totals; these are operational application metrics, not project schedule metrics.
- **Scope/change clues:** role migration and booking constraint migration capture two technical changes.

### Evidence absent

No task board, sprint burndown, milestone register, Gantt chart, WBS, earned-value data, budget, timesheets, defect log, issue/PR export, code coverage, static-analysis report, CI workflow, release pipeline, environment promotion record, performance monitoring, risk register, formal change log, or communication record is present.

Consequently:

- Git activity cannot be used for progress or contribution measurement because the workspace is not a Git repository.
- Passing tests do not demonstrate full requirement completion or stakeholder approval.
- File modification dates can describe this snapshot's chronology but not planned-versus-actual schedule performance.
- There is no factual basis for sprint velocity, schedule variance, cost variance, defect density, availability, or customer satisfaction numbers.

## 7. Project Closure and Current Status

### Current technical status

**VERIFIED:** The source is substantial and a packaged JAR exists. Stored automated tests passed for their limited targets. Core modules and views exist. This supports the conclusion that TravelGO is a developed university-project prototype/demonstrator.

**NOT VERIFIED:** It is not possible to declare the project production-ready, formally complete, deployed, accepted, or closed.

### Delivered versus outstanding

Delivered in source: the modules enumerated in Section 3, local SQL Server configuration, seed data, static assets, PDFs, SMTP paths, and administrative diagrams.

Outstanding/high-risk work includes securing uploaded documents, replacing simulated payment with verified server-to-server gateway processing, removing demo credentials, enforcing or simplifying granular permissions, adopting controlled migrations, expanding testing, adding monitoring/logging, production deployment configuration, and reconciling stale documentation.

### Final testing and readiness

The latest stored reports total eight passing executions. There are no failed executions in the retained XML, but no comprehensive suite run is proven because the current dashboard class has more methods than the report. No UAT, security scan, penetration test, performance test, accessibility audit, data migration test, disaster-recovery test, or production smoke test exists.

### Maintenance considerations

Maintain schema changes with Flyway/Liquibase; externalize all credentials; replace local uploads with authorized object/document storage; add observability and audit trails; document backup/restore; keep Spring/iText/JDBC dependencies patched; test state transitions and ownership centrally; and resolve source/template encoding artifacts. The generated source scripts should be treated cautiously because ownership and continued purpose are undocumented.

### Closure confirmation required

The student must supply evidence of final demonstration, sponsor/client or lecturer acceptance, scope sign-off, unresolved defect disposition, handover, repository/tag/release identification, deployment decision, archived documents, and a formal lessons-learned/closure meeting if these occurred.

## 8. Eight Project Performance Domains Analysis

PMBOK 7 performance domains are interacting areas of project activity; they are not sequential process groups. The following matrix distinguishes repository evidence from interpretations.

| Domain | Definition/relevance | Project-specific application and evidence | Positive outcomes | Problems/limitations and missing evidence | Student information required |
|---|---|---|---|---|---|
| 1. Stakeholders | Activities for productive relationships with people affected by the project | User categories are embodied in `SecurityConfig`, controllers, role entities, and tailored views. Public inquiries and reviews could collect user input. | Different operational needs are represented in workflows. | Code proves user roles, not stakeholder identification or engagement. No register, influence analysis, interviews, feedback log, sponsor/client evidence, or acceptance. | Identify client/sponsor/lecturer/team; explain elicitation, communication, feedback, conflict, and acceptance. |
| 2. Team | Conditions, culture, leadership, roles, collaboration, and shared ownership | Layered code and module boundaries could support division of work. `.gitignore` shows version-control intent. | Modular packages make parallel feature ownership feasible. | No Git metadata, contributors, RACI, team charter, meeting records, peer review, conflict handling, or skills plan. | Team size/names, roles, task ownership, collaboration tools, decision method, leadership and conflict examples. |
| 3. Development Approach and Life Cycle | Selection and cadence of predictive, adaptive, or hybrid delivery | File dates and migrations suggest increments; tests accompany later workflow/profile changes. Architecture is a single deployable increment. | Iteration appears to have allowed UI and business-rule refinement. | Scrum/Agile cannot be verified; no sprint/backlog/release plan. Timestamps are not process records. | State actual method, iteration length, lifecycle phases, rationale, ceremonies, and how increments were accepted. |
| 4. Planning | Organizing scope, schedule, resources, quality, risk, procurement, and communications | Technical planning is visible in architecture, entity model, roles, enums, PlantUML, and dependency selection. | Coherent layered design and domain statuses provide a useful technical baseline. | No approved scope, WBS, estimates, schedule, resources, budget, quality plan, test plan, risk plan, communications plan, or traceability. | Provide plans/baselines, technology rationale, milestones, resource constraints, risks, and changes to plan. |
| 5. Project Work | Establishing processes and executing work to produce outcomes | 102 main Java files, 50 templates, integration services, configuration, test code, migrations, and build artifact provide direct work evidence. | A broad demonstrator with linked customer/staff/admin workflows exists. | No work authorization, task ownership, supplier management, code review, definition of done, or effort records. Several production gaps remain. | Describe allocation, reviews, integration practice, troubleshooting, environments, and actual workflow. |
| 6. Delivery | Producing scope and quality that realize intended value | Catalogue, booking, visa, payment-record, ticket, inquiry, admin, settings, and report deliverables exist. | Cross-role workflow can demonstrate an end-to-end travel-agency scenario. | Value realization and acceptance are unmeasured; gateway/embassy/ticket integrations are absent; formal completion unknown. | Confirm agreed MVP, completed/deferred requirements, demo outcome, UAT, acceptance, handover, and benefits. |
| 7. Measurement | Assessing performance and taking action using meaningful indicators | Eight stored passing test executions, file/module counts, build artifact, and application dashboard aggregates are measurable evidence. | Some regression controls and operational summaries exist. | No coverage, baseline, velocity, planned/actual data, defect trend, quality threshold, benefits KPI, cost/schedule variance, or full-suite proof. | Provide planned/actual dates, task completion, defect/test totals, review results, KPI targets, and reporting cadence. |
| 8. Uncertainty | Navigating threats, opportunities, ambiguity, complexity, and risk | Transactions, state checks, ownership checks, email fallback, maintenance mode, and migrations are technical responses. | Several predictable failure modes are guarded. | No formal risk process. Significant payment, privacy, deployment, dependency, schedule, and requirements risks remain; contingency ownership is unknown. | Provide risk register/meetings, probability-impact decisions, mitigations, realized issues, contingency use, and residual risk acceptance. |

### Domain-level conclusion

The repository provides strongest evidence for **Project Work** and technical aspects of **Delivery**. It provides partial evidence for technical **Planning**, **Measurement**, and **Uncertainty** responses. It provides little reliable evidence of formal **Stakeholder**, **Team**, lifecycle governance, or value-acceptance activity. The final report should present that imbalance honestly and use student records to fill it.

## 9. Application of Twelve PM Principles

The principles guide behavior and decisions; code can demonstrate some engineering expressions but cannot prove interpersonal conduct or governance by itself.

| Principle | Meaning in project management | Possible project application and concrete evidence | Outcome/benefit | Limitation/missing information | Confirmation questions |
|---|---|---|---|---|---|
| 1. Stewardship | Responsible, ethical, diligent care of resources and impacts | BCrypt, ignored local secret file, ownership checks, transactions, and status gates show some care (`SecurityConfig`, `.gitignore`, services). | Protects credentials/records and invalid transitions to a degree. | Public visa files, demo credentials, fake payment semantics, and absent privacy policy weaken stewardship. | Who owned ethics/privacy/security? Were credentials rotated and data consent/retention agreed? |
| 2. Team | Create a collaborative, accountable team environment | Package/module separation could support shared ownership. | Clear technical boundaries can reduce coordination friction. | No proof of roles, collaboration, psychological safety, reviews, mentoring, or conflict handling. | Who did what, how was work integrated/reviewed, and how were disagreements resolved? |
| 3. Stakeholders | Engage stakeholders proactively and adapt to their interests | Multiple role-specific experiences and public inquiry/review mechanisms indicate awareness of distinct users. | Solution covers customer, operations, visa, and admin perspectives. | No evidence these needs came from real engagement. | Who was consulted, how often, what feedback changed scope, and who accepted it? |
| 4. Value | Continually focus on outcomes and benefits | Integrated booking-to-ticket flow targets operational coordination; reports and dashboards support decisions. | Potentially reduces manual handoffs and improves visibility. | No benefit measures or actual-user evidence; integrations are simulated/local. | What value proposition and KPIs were approved? Were benefits observed? |
| 5. Systems Thinking | Understand interactions, feedback, and downstream effects | State dependencies span booking, visa, payment, refund, notification, and ticket services. | Prevents some locally valid but globally invalid actions. | State history is not recorded; failures and compensation across email/files/DB are incomplete. | How were end-to-end dependencies modeled and tested? What external systems were considered? |
| 6. Leadership | Adapt leadership behaviors to guide outcomes | Technical decisions and explanatory comments imply direction, but authorship is unavailable. | A consistent architecture emerged. | Leadership cannot be inferred from code. | Who led which areas, how were decisions made, and give a documented leadership example. |
| 7. Tailoring | Adapt method and governance to context | A simple modular monolith, Maven, local run script, and targeted tests fit a university prototype. | Lower operational overhead and faster demonstration. | No tailoring rationale or comparison with alternatives; some prototype choices are unsafe for production. | Why this lifecycle, documentation level, database, and test depth were selected? |
| 8. Quality | Build quality into processes and deliverables | Validation, constraints, transactions, tests, PDF output, and role checks are present. | Improves correctness of selected critical paths. | Very limited test evidence; no coverage, review checklist, NFR acceptance, static/security testing. | What was the definition of done and quality threshold? What manual/UAT tests occurred? |
| 9. Complexity | Recognize and navigate human, technical, and systemic complexity | Enums and services model the multi-stage visa/payment/ticket process; migrations respond to evolving states. | Makes workflow complexity explicit. | Complexity analysis and simplification decisions are undocumented; status logic is distributed. | What complexity caused rework and how was it managed? |
| 10. Risk | Continually evaluate threats/opportunities and responses | Ownership checks, BCrypt, transaction boundaries, deadlines, maintenance mode, fallback email, and migration code are responses. | Reduces selected security, consistency, and availability risks. | No risk register/owners; critical document/payment risks remain. | Which risks were formally identified, rated, assigned, mitigated, and reviewed? |
| 11. Adaptability and Resilience | Respond to change and recover from setbacks | Role migration, booking constraint migration, email fallback, and configurable maintenance mode show adaptation. | Preserves compatibility and degraded operation. | No backup/recovery, rollback, incident drill, feature flags, or change retrospectives. | Which changes/incidents occurred and what contingency actions worked? |
| 12. Change | Enable adoption and transition from current to future state | Role migration and maintenance/settings mechanisms manage limited technical change. | Can ease data-model evolution and controlled downtime. | No organizational change, training, rollout, resistance, communication, or adoption evidence. | How were users trained, releases communicated, feedback handled, and adoption confirmed? |

### Principle-level conclusion

Systems thinking, technical quality, risk responses, and adaptability have concrete implementation examples. Stewardship is mixed because protective controls coexist with serious demonstrator shortcuts. Claims about team culture, leadership, stakeholder engagement, and organizational change must come from the team, not be reverse-engineered from code.

## 10. Lessons Learned

| Area / situation | Root cause (where identifiable) | Action/result evidenced | Lesson and future recommendation | Evidence / confirmation |
|---|---|---|---|---|
| Booking state expanded to include paid-awaiting-ticket release | Database check constraint lagged behind enum/workflow change | Startup migration drops/recreates constraint | Treat schema and code as one versioned change; use Flyway/Liquibase and rollback tests | `BookingStatusConstraintMigration`; confirm incident context |
| Staff role changed from Travel Consultant to Package Manager | Domain terminology evolved | Idempotent startup migration reassigns users/deletes or renames legacy role | Model role changes explicitly and update docs/tests/security together | `DataInitializer`; `SecurityConfig` still retains legacy compatibility |
| Customer profile must not trust submitted user identity | Horizontal-authorization risk | Controller uses authenticated email; unit tests verify behavior | Derive identity server-side and test ownership for every customer endpoint | `CustomerProfileControllerTest` |
| Full payment and ticket release need separate states | Premature access to tickets/business details | `PAID_AWAITING_TICKETS`; service test verifies officer release after payment | Explicit intermediate states make approvals auditable and safer | Booking/payment services and test; broader e2e test needed |
| Visa/payment stages are interdependent | Multi-entity workflow complexity | Service rules gate processing/payment/ticket transitions | Centralize a state-transition policy and test all valid/invalid transitions | Services; only two payment transitions currently tested |
| Email is an unreliable side effect | SMTP can fail after database work | Inquiry replies/ticket releases tolerate email failure and retain in-app result | Use queued/outbox delivery with retry and delivery status | Email/contact/staff visa services; operational results not verified |
| Local file upload is simple but unsafe | Prototype-focused implementation | UUID prefix, size/extension validation, direct static mapping | Confidential documents require authenticated streaming, content inspection, safe names, and retention controls | Visa controller and web/security config |
| Payment-like UI can overstate completion | No gateway integration | Local payment records are immediately marked paid | Label simulations clearly; validate amount server-side; use gateway webhooks/idempotency before claiming payment capability | Customer payment controller/service/template |
| Documentation can become stale | Stack and role changes were not synchronized | Current walkthrough still claims H2/MySQL and old role | Add documentation review to definition of done | Walkthrough vs POM/properties/initializer |
| Testing focused on recent defects/critical rules | Limited time or incremental fixes (**INFERRED**) | Eight recorded passing tests target profile/payment/ticket/context/rendering | Build a risk-based test pyramid, full-suite CI, coverage thresholds, and test-data isolation | Test sources/Surefire; student should confirm planning rationale |
| Version-control evidence must be preserved | Workspace copy lacks `.git` | No recovery possible from current snapshot | Include repository metadata/export, tags, PRs, and issue history in academic handover | No `.git`; confirm canonical remote/repository |
| Formal PM evidence cannot be reconstructed from code | Engineering artifacts were retained more than governance artifacts | PM domains have large evidence gaps | Maintain lightweight charter, backlog, risk/change/decision logs, sprint reviews, and acceptance records alongside code | Artifact search; team confirmation required |

These lessons do not claim undocumented incidents. Where causality is not directly visible, it is labeled inferred or requires confirmation.

## 11. Future Work

| Priority | Improvement | Reason and expected benefit | Related module | Complexity |
|---|---|---|---|---|
| High | Protect visa documents with authenticated controller/object storage and per-record authorization | Removes critical public exposure; supports privacy and auditing | Uploads/security/visa | Medium–High |
| High | Integrate a real payment provider; calculate payable amount server-side; use signed webhooks and idempotency | Prevents tampering and false paid status; supports real settlement | Payments/bookings/visas | High |
| High | Remove seeded/prefilled credentials and rotate all environment secrets | Prevents unauthorized access and credential leakage | Auth/config/initializer/templates | Medium |
| High | Add end-to-end authorization and workflow tests, full-suite CI, security scanning, and coverage reporting | Detects regressions and establishes objective quality evidence | All modules | Medium–High |
| High | Introduce versioned migrations and production profiles; disable uncontrolled schema update | Makes releases reproducible and reversible | Database/deployment | Medium |
| High | Resolve refund correctness: select the correct paid visa transaction, handle absent payment, and separate record from actual payout | Prevents null/incorrect refunds and misleading completion claims | Visa/refund/payment | Medium |
| High | Add server-side validation for all monetary/status inputs and centralize authorization/state transitions | Reduces tampering and distributed-rule defects | Controllers/services | Medium |
| Medium | Enforce granular permissions or remove the misleading permission editor | Aligns authorization behavior with admin configuration | RBAC | Medium |
| Medium | Add audit logs for account, visa, payment, refund, settings, and ticket actions; wire status history | Improves accountability and reporting | Cross-cutting/visa | Medium–High |
| Medium | Implement scheduled expiry and reminders | Makes declared deadlines operational | Booking/visa/payment/notification | Medium |
| Medium | Replace console output with structured, redacted logs and observability/health metrics | Supports diagnostics without leaking data | Cross-cutting | Medium |
| Medium | Add pagination, database indexes, aggregate queries, and load tests | Avoids `findAll()` bottlenecks as data grows | Repositories/dashboards/reports | Medium |
| Medium | Move email to an outbox/queue with retries and delivery status | Prevents silent divergence between saved replies and delivered messages | Email/inquiries/tickets | Medium–High |
| Medium | Add backup/restore, retention/deletion, privacy/consent, and incident procedures | Improves operational and legal readiness | Data/operations | Medium |
| Medium | Create Docker/cloud deployment, TLS, secret manager, environments, automated release/rollback | Enables repeatable deployment | Deployment | High |
| Medium | Reconcile walkthrough, database profile, roles, and user documentation | Prevents incorrect setup/report claims | Documentation | Low |
| Low | Accessibility review, keyboard testing, semantic/contrast improvements | Broadens usability and provides measurable UX quality | UI | Medium |
| Low | Add REST/mobile API only if a validated stakeholder need exists | Enables new channels without speculative scope | Architecture | High |
| Low | Add maps, real hotel/flight inventory, SMS, and richer analytics only after value validation | Extends business capability | External integration | High |

None of these recommendations is already implemented unless explicitly described elsewhere as partial groundwork.

## 12. References and Available Appendices

### Existing artifacts suitable for appendices

| Artifact | Exact path | Use and caution |
|---|---|---|
| Detailed admin sequence diagram | `travelgo-admin-sequence.puml` | Architecture/sequence appendix; validate labels against current code/database |
| Simplified admin sequence diagram | `travelgo-admin-simple.puml` | Concise admin flow; currently labels database MySQL although active runtime is SQL Server |
| Diagram viewer | `admin-sequence-diagram-viewer.html` | Presentation aid for diagram |
| Walkthrough | `src/main/resources/templates/walkthrough.md` | Development narrative; stale stack/role/credential claims must be corrected/redacted |
| Maven build definition | `pom.xml` | Authoritative dependency/version appendix |
| Active configuration | `src/main/resources/application.properties` | Environment appendix with all credentials/secret values redacted |
| Alternate DB configuration | `src/main/resources/application-mysql.properties` | Demonstrates intent only; MySQL driver is absent |
| Security design | `src/main/java/com/travelgo/config/SecurityConfig.java` | Role/access and authentication evidence |
| Domain model | `src/main/java/com/travelgo/entity/` | Basis for an ER/class diagram; no complete existing ER diagram found |
| Controllers/services | `src/main/java/com/travelgo/controller/`, `src/main/java/com/travelgo/service/` | Functional and workflow evidence |
| Test sources | `src/test/java/com/travelgo/` | Test appendix |
| Test results | `target/surefire-reports/TEST-*.xml`, `target/surefire-reports/*.txt` | Eight stored passing executions; generated artifact timestamps apply |
| Packaged release candidate | `target/travelgo-0.0.1-SNAPSHOT.jar` | Local build evidence, not proof of deployment |
| UI templates | `src/main/resources/templates/` | Screenshot source; actual screenshots are not present as a formal appendix |

There is no use-case diagram, complete ER diagram, class diagram, requirements specification, API documentation, test plan/report beyond Surefire, sprint record, risk register, Gantt chart, WBS, meeting record, deployment document, or formal acceptance record.

### Technologies requiring credible citations in the final academic report

Use current official documentation or academic sources for PMBOK 7, Spring Boot 4, Spring Security, Spring MVC, Thymeleaf, Spring Data JPA/Hibernate, Jakarta Validation, Microsoft SQL Server/JDBC, Maven, JUnit 5, Mockito, iText 7, SMTP/email, BCrypt, and relevant OWASP guidance. This pack deliberately does not invent publication details or URLs. Licensing implications of iText 7 should also be checked for the intended distribution model.

## 13. Git History and Development Timeline

### Git limitation

**VERIFIED:** `G:\a\ISE3.0` has no `.git` directory and Git commands report “not a git repository.” Therefore commit IDs, authors, branches, merges, tags, pull requests, reviews, commit dates, and contribution statistics are unavailable. `.gitignore` and `.gitattributes` only show that Git use was intended. The canonical repository/export must be supplied for a genuine Git history.

### Snapshot-based timeline (not a Git timeline)

| Date | Evidence visible in file metadata | Safe interpretation |
|---|---|---|
| 20 Aug 2026 | Maven wrapper, HELP, base test, early admin assets/properties | Project scaffolding/admin work existed by this date |
| 21 Aug 2026 | Early admin templates and walkthrough | Admin/auth documentation and UI iteration |
| 24 Aug 2026 | Entity-generation scripts | Automated entity scaffolding was prepared; whether executed is not verified |
| 27 Aug 2026 | `.gitignore` and local properties | Secret-exclusion and local database configuration work |
| 9–23 Sep 2026 | Authentication/destination/customer media; categories/staff templates | UI/catalogue asset expansion |
| 3 Oct 2026 | Staff edit/report templates and scroll gallery assets | Admin/report/UI refinement |
| 9 Oct 2026 | Large group of Java sources and core POM/config timestamps | Major backend/application consolidation in this workspace copy |
| 12–13 Oct 2026 | Further workflow/UI files, video, and PlantUML admin diagrams | Workflow presentation/documentation work |
| 14 Oct 2026 | `run.ps1` | Local execution support |
| 16–17 Oct 2026 | Workflow services/controllers, migration, tests, UI refinements; JAR built 17 Oct | Business-rule hardening and packaging |
| 19 Oct 2026 | Profile/customer UI and test report updates; final public styling | Latest visible verification and presentation refinement |

File modification times can be copied, reset, or produced by extraction. They are **LOW-to-MEDIUM confidence chronological clues**, not proof of project milestones, effort, authorship, sprint completion, or approval.

## 14. Project Evidence Matrix

| Report section | Finding | Evidence source / exact path or reference | Confidence | Missing information / required confirmation |
|---|---|---|---|---|
| Overview | Name and purpose are TravelGO smart travel-agency management university project | `pom.xml`; `src/main/resources/application.properties` | HIGH | Confirm preferred report title and client context |
| Problem/value | Integrated workflows address fragmented booking/visa/payment coordination | Services/controllers across `src/main/java/com/travelgo/` | MEDIUM | Original problem statement, baseline and benefits |
| Architecture | One Spring MVC/Thymeleaf modular monolith | `pom.xml`; application class; controllers/templates | HIGH | Architecture approval and rationale |
| Database | Active database is SQL Server | `pom.xml`; `application.properties` | HIGH | Production database/environment, if any |
| Documentation accuracy | Walkthrough/PlantUML MySQL/H2 claims conflict with current implementation | `walkthrough.md`; `travelgo-admin-simple.puml`; `pom.xml` | HIGH | Which configuration was demonstrated/submitted? |
| Authentication | Session form login and BCrypt strength 10 | `SecurityConfig.java`; `CustomUserDetailsService.java` | HIGH | Security review/UAT results |
| Authorization | Role URL rules plus customer ownership checks | `SecurityConfig.java`; customer controllers | HIGH | Negative authorization testing beyond profile |
| Permissions | Permission records are editable but not used in runtime authorities | `RoleService.java`; search absence of authority checks | HIGH | Was fine-grained enforcement deferred intentionally? |
| Data model | 19 entities plus role-permission join table and key uniqueness constraints | `src/main/java/com/travelgo/entity/` | HIGH | Approved ERD/data dictionary |
| Booking | State rules, server-side initial price calculation, ticket gate | booking controller/service/entity | HIGH implementation; MEDIUM behavior | Full e2e/UAT and concurrent capacity tests |
| Visa | Application/document/charge/process/decision workflow exists | visa controllers/services/entities | HIGH implementation | Embassy integration and complete transition tests |
| Payment | Two-stage record flow exists; two service tests pass | `PaymentService.java`; `PaymentServiceTests.java`; Surefire XML | HIGH for tested cases | Real settlement/gateway and amount-integrity acceptance |
| Ticket | Paid gate tested; PDF service and owner endpoint exist | `BookingServiceTicketReleaseTests`; booking controller/PDF service | HIGH for service rule | PDF content/email/e2e test |
| Upload security | Visa files are publicly served under `/uploads/**` | `SecurityConfig.java`; `WebConfig.java`; visa templates/controller | HIGH | Risk acceptance/remediation |
| Payment limitation | Client amount/type are trusted and payment is marked paid without provider | customer payment controller/service/template | HIGH | Confirm that it is explicitly a simulation |
| Email | SMTP implementation and failure handling exist | `EmailService.java`; `ContactMessageService.java` | HIGH implementation | Delivery test and production sender configuration |
| Reporting | Eight PDF endpoints backed by iText report generation | `ReportController.java`; `ReportService.java` | HIGH implementation | Accuracy, access, volume tests |
| Testing | Stored results: 8 tests, zero failures/errors/skips | `target/surefire-reports/TEST-*.xml` dated 19 Oct 2026 | HIGH | Full suite run, coverage, UAT, manual/security/performance tests |
| Test limitation | Current dashboard class has nine methods; report contains one | source vs `TEST-com.travelgo.DashboardRenderingTests.xml` | HIGH | Explain selective execution and provide full run |
| Build | Executable JAR produced 17 Oct 2026 | `target/travelgo-0.0.1-SNAPSHOT.jar` | HIGH | Reproducible clean build/release tag |
| Deployment | Local run script and localhost bind only | `run.ps1`; application properties | HIGH | Hosting, TLS, environments, deployment evidence |
| Git | No repository metadata in workspace | Git command result; absence of `.git` | HIGH | Canonical repository URL/export and history |
| Methodology | Iterative/hybrid is plausible from dated changes | file timestamps, migrations, tests | LOW–MEDIUM | Actual methodology, sprints, ceremonies |
| Monitoring | Only build/test artifacts and in-app metrics found | Surefire, dashboard controllers | HIGH | PM tracking reports and monitoring tools |
| Risk | Technical controls exist but no risk register | security/services; artifact search | HIGH | Formal risks, owners, reviews and responses |
| Closure | No acceptance/handover/closure record | workspace-wide artifact search | HIGH | Demonstration, sign-off, final status |

## 15. Missing Information and Student Questions

### CRITICAL — required to write an accurate final report

1. What exact project title should appear on the report, and is TravelGO the official agreed name?
2. Who was the client/sponsor/product owner, if any, and what organizational problem did they state?
3. What were the approved objectives, in-scope/out-of-scope items, functional requirements, and acceptance criteria?
4. How many team members participated, what are their names/roles, and which modules/tasks did each own?
5. What were the actual start date, planned completion date, actual completion date, and project duration?
6. What methodology was actually followed? If Agile/Scrum is claimed, provide sprint dates, goals, backlog evidence, reviews, retrospectives, and role assignments.
7. Where is the canonical Git repository? Provide log/branches/tags/PRs/issues or an export so the development timeline and contributions can be verified.
8. Was the online payment screen explicitly presented as a simulation? Was any real gateway ever used outside this snapshot?
9. Was the system demonstrated or deployed, on what environment, with what database, and what was the result?
10. What testing was performed beyond the eight stored executions—manual tests, UAT, security, integration, accessibility, performance—and what were the results?
11. Who formally accepted the deliverables, when, and against which criteria? If not accepted, what is the current project status?
12. Are the uploaded files synthetic test data, and what privacy/retention/consent controls were agreed?
13. Which requirements remain incomplete or were removed, and who approved those scope decisions?

### IMPORTANT — needed for robust PMBOK and execution analysis

1. Provide the project charter/proposal, requirement notes, use cases/user stories, WBS, Gantt/schedule, milestone list, and planned-versus-actual dates.
2. What communication tools, meeting cadence, decision process, and reporting format did the team use?
3. How were tasks allocated and progress tracked? Supply board screenshots/exports if available.
4. What budget, hardware, software, hosting, time, and human-resource constraints applied?
5. Why were Spring Boot, Thymeleaf, SQL Server, iText, and a monolith selected? What alternatives were evaluated?
6. What quality criteria/definition of done and code-review process were used?
7. What were the major technical/team/schedule challenges, root causes, actions, results, and owners?
8. What risks were identified, how were probability/impact and owners recorded, and which risks materialized?
9. What stakeholder/lecturer/client feedback or change requests altered requirements, roles, UI, or workflows?
10. Explain the role migration and booking-status migration: when, why, who decided, and what issue triggered each?
11. Explain the selective dashboard test report and provide a recent clean full-suite result if available.
12. What defects remain, how severe are they, and which were accepted/deferred?
13. Was there a final handover, user training, operations guide, backup plan, maintenance ownership, or support agreement?

### OPTIONAL — useful enrichment

1. Provide UI screenshots, demo script, presentation slides, lecturer comments, and user feedback.
2. Provide estimates or actual hours by phase/module, if reliably recorded.
3. Provide team retrospective notes and individual learning reflections.
4. Identify desired future integrations, expected user volume, browser/device targets, and hosting preference.
5. Provide source/licensing records for images/video and confirm iText licensing suitability.
6. Confirm whether generated entity scripts were used and whether their generated output was manually adapted.

## 16. Executive Summary for Academic Report Generation

TravelGO is a substantial university-project prototype of a smart travel-agency management system. It is implemented as one Java 17/Spring Boot 4.0.7 modular monolith using Spring MVC, Thymeleaf, Spring Security, Spring Data JPA/Hibernate, Microsoft SQL Server, Spring Mail, and iText 7. Its domain spans public package discovery, customer registration/profile/bookings/travelers/hotels/visas/documents/payment records/notifications/reviews/ticket PDFs, staff catalogue/booking/inquiry operations, visa-officer workflows, and administrator account/RBAC/settings/reporting functions. The codebase contains 102 main Java files, 19 entities, 19 repositories, 23 services, 25 controllers, and 50 HTML templates.

The main implemented value chain is booking → visa application/document review → visa fee record → processing/decision → full-package payment record → officer ticket release. Services enforce several meaningful rules, including customer ownership, future travel dates/capacity, document-before-charge, visa-payment-before-processing, visa-approval-before-full-payment, and verified full-payment-before-ticket release. Security uses form login, BCrypt, sessions, role-based URL rules, and customer ownership checks. The architecture is understandable and suitable for a university demonstration.

Functional confidence must be carefully bounded. Stored Surefire artifacts dated 19 October 2026 show eight passing executions with no failures/errors/skips: three customer-profile tests, two payment-transition tests, one ticket-release test, one context-load test, and one customer workflow rendering test. The current dashboard test class contains nine methods, while its retained report proves only one. There is no coverage report, full end-to-end/UAT record, performance test, security assessment, accessibility audit, or production smoke test.

The two most important implementation limitations are production-critical. First, visa documents are stored locally and publicly served because `/uploads/**` is permitted without authentication. Second, the polished payment UI is a simulation: the server accepts the client-supplied type and amount and immediately marks a record paid without a payment gateway, server-side amount derivation, or webhook verification. Other limitations include embedded demo credentials (not reproduced here), unenforced granular permissions, no scheduled expiry, no wired visa status audit history, simulated refunds, startup seed/schema mutation, limited structured logging/monitoring, and local-only deployment evidence.

Project-management evidence is much weaker than engineering evidence. No `.git` repository is present, so commits, contributors, branches, pull requests, reviews, and an authentic Git timeline cannot be reported. No charter, stakeholder register, formal requirements, WBS, Gantt, backlog, sprint records, meeting minutes, risk/change/decision log, budget, task assignment, acceptance, deployment, handover, or closure record was found. File dates suggest incremental work from August to October 2026, but they are not substitutes for approved milestones or Git history. Therefore the final academic report should describe an inferred iterative/hybrid development pattern only if the student confirms it; it must not claim Scrum or formal closure from source code alone.

Against PMBOK 7, the repository strongly supports Project Work and partial technical Delivery, while it only partially evidences Planning, Measurement, and Uncertainty responses and cannot prove Stakeholder engagement, Team practices, leadership, or formal lifecycle governance. The 12 principles have credible technical examples in systems thinking, quality, risk response, adaptability, and limited stewardship, but human/management application requires team testimony. Before report generation, the student should provide the original problem and requirements, stakeholder/client identity, team roles, methodology, dates/milestones, planning and monitoring records, risk/change history, test/UAT outcomes, deployment/demo result, acceptance, and lessons learned.

**Overall audit conclusion:** TravelGO is a broad, demonstrable, server-rendered academic system with coherent domain modeling and selected tested workflow protections. It should be reported as a developed prototype whose core source implementation is evident, not as a fully verified production system or formally closed project. The final report can be strong if it combines this technical evidence with authentic team/project-management records and explicitly acknowledges the security, integration, testing, deployment, and evidence gaps above.
