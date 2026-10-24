package com.travelgo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Singleton configuration row storing application-wide system and company settings.
 * Only one row exists in this table (id = 1).
 * 
 * [HOW IT WORKS IN THE APP]:
 * - Controls site-wide maintenance mode (websiteStatus = "ACTIVE" vs "MAINTENANCE").
 * - Stores company header/footer data (company name, email, phone, business hours, currency).
 */
@Entity
@Table(name = "system_settings")
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "company_description", columnDefinition = "TEXT")
    private String companyDescription;

    @Column(name = "company_email", length = 150)
    private String companyEmail;

    @Column(name = "company_phone", length = 20)
    private String companyPhone;

    @Column(name = "company_address", columnDefinition = "TEXT")
    private String companyAddress;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    // Stored as formatted string: "Mon - Fri: 9:00 AM - 6:00 PM | Sat: 10:00 AM - 4:00 PM"
    @Column(name = "business_hours", columnDefinition = "TEXT")
    private String businessHours;

    /** System status: "ACTIVE" or "MAINTENANCE" */
    @Column(name = "website_status", length = 20, nullable = false)
    private String websiteStatus = "ACTIVE";

    @Column(name = "maintenance_message", columnDefinition = "TEXT")
    private String maintenanceMessage;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 10)
    private String currency = "USD";

    @Column(length = 50)
    private String timezone = "UTC";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Updates the timestamp whenever an admin saves modified system configurations.
     */
    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---- Constructors ----

    /** Default constructor required by JPA */
    public SystemSettings() {}

    // ---- Getters and Setters ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getWebsiteStatus() {
        return websiteStatus;
    }

    public void setWebsiteStatus(String websiteStatus) {
        this.websiteStatus = websiteStatus;
    }

    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }

    public void setMaintenanceMessage(String maintenanceMessage) {
        this.maintenanceMessage = maintenanceMessage;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
