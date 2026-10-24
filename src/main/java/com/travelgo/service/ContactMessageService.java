package com.travelgo.service;

import com.travelgo.entity.ContactMessage;
import com.travelgo.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactMessageService {

    private final ContactMessageRepository repository;
    private final EmailService emailService;

    public ContactMessageService(ContactMessageRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public List<ContactMessage> findAll() {
        return repository.findAll();
    }

    public Optional<ContactMessage> findById(Long id) {
        return repository.findById(id);
    }

    public ContactMessage save(ContactMessage entity) {
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public ContactMessage replyToMessage(Long messageId, String replyText) {
        ContactMessage msg = repository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Contact message not found"));
        msg.setReplyText(replyText);
        msg.setRepliedAt(java.time.LocalDateTime.now());
        msg.setStatus(com.travelgo.enums.ContactMessageStatus.RESOLVED);
        ContactMessage saved = repository.save(msg);

        // Send the reply to the customer's email via Gmail SMTP (Branded HTML Email)
        try {
            emailService.sendReplyEmail(
                    msg.getSenderEmail(),
                    msg.getSenderName(),
                    msg.getSubject(),
                    msg.getMessage(),
                    replyText
            );
        } catch (Exception e) {
            // Log the error but don't fail the reply save — email is a best-effort addition
            System.err.println("Failed to send email to " + msg.getSenderEmail() + ": " + e.getMessage());
        }

        return saved;
    }
}
