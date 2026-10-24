package com.travelgo.entity;

import com.travelgo.enums.VisaStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit log entry that records every status change in a visa application lifecycle.
 * Provides transparency so admins and officers can inspect who approved/rejected an application and when.
 */
@Entity
@Table(name = "visa_status_history")
public class VisaStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Links each history audit entry to the visa application being modified.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visa_application_id", nullable = false)
    private VisaApplication visaApplication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisaStatus status;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    /** Staff member / Officer who initiated this status transition */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id")
    private User changedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    /**
     * Automatically captures the timestamp when the status was updated.
     */
    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public VisaApplication getVisaApplication() { return visaApplication; }
    public void setVisaApplication(VisaApplication visaApplication) { this.visaApplication = visaApplication; }
    public VisaStatus getStatus() { return status; }
    public void setStatus(VisaStatus status) { this.status = status; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
