package com.travelgo.entity;

import com.travelgo.enums.ContactMessageStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents public contact inquiries and support tickets sent via the website's contact form.
 */
@Entity
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optional relationship if the inquiry was sent by an authenticated user */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @jakarta.validation.constraints.NotBlank(message = "Name is required")
    @jakarta.validation.constraints.Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String senderName;

    @jakarta.validation.constraints.NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Please enter a valid email address")
    @Column(nullable = false)
    private String senderEmail;

    @jakarta.validation.constraints.NotBlank(message = "Subject is required")
    @jakarta.validation.constraints.Size(max = 200, message = "Subject cannot exceed 200 characters")
    @Column(nullable = false)
    private String subject;

    @jakarta.validation.constraints.NotBlank(message = "Message is required")
    @jakarta.validation.constraints.Size(min = 10, max = 5000, message = "Message must be between 10 and 5000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Ticket status: OPEN, IN_PROGRESS, RESOLVED, CLOSED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactMessageStatus status = ContactMessageStatus.OPEN;

    /** Staff member assigned to resolve this inquiry */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String replyText;

    private LocalDateTime repliedAt;

    /**
     * Sets creation timestamp on new inquiry submission.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates timestamp when staff replies or reassigns the ticket.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public ContactMessageStatus getStatus() { return status; }
    public void setStatus(ContactMessageStatus status) { this.status = status; }
    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getReplyText() { return replyText; }
    public void setReplyText(String replyText) { this.replyText = replyText; }
    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }
}
