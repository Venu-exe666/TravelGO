package com.travelgo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a specific file document (e.g. Passport scan, Photo, Bank statement)
 * uploaded by a customer for their visa application.
 */
@Entity
@Table(name = "visa_documents")
public class VisaDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Multiple documents (passport, photo, financials) belong to a single visa application.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visa_application_id", nullable = false)
    private VisaApplication visaApplication;

    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String documentUrl;

    /** Verification flag set by Visa Officer after reviewing the file */
    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Records the exact date and time the file was uploaded.
     */
    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public VisaApplication getVisaApplication() { return visaApplication; }
    public void setVisaApplication(VisaApplication visaApplication) { this.visaApplication = visaApplication; }
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { this.isVerified = verified; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
