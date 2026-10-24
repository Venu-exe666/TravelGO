package com.travelgo.service;

import com.travelgo.entity.Booking;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Year;

/**
 * Service responsible for sending professional branded HTML emails via Gmail SMTP.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Sends an advanced luxury HTML reply email to the customer when staff responds to their inquiry.
     *
     * @param toEmail         the customer's email address
     * @param customerName    the customer's name
     * @param subject         the inquiry subject line
     * @param originalMessage the customer's original inquiry/question
     * @param replyBody       the staff member's reply text
     */
    public void sendReplyEmail(String toEmail, String customerName, String subject, String originalMessage, String replyBody) {
        String safeCustomerName = (customerName != null && !customerName.isBlank()) ? customerName.trim() : "Valued Traveler";
        String safeSubject = (subject != null && !subject.isBlank()) ? subject.trim() : "Tour Inquiry";
        String safeReplyBody = (replyBody != null) ? replyBody.trim() : "";
        String safeOriginalMessage = (originalMessage != null) ? originalMessage.trim() : "";

        // Render HTML template via Thymeleaf
        String htmlContent;
        try {
            Context context = new Context();
            context.setVariable("customerName", safeCustomerName);
            context.setVariable("subject", safeSubject);
            context.setVariable("originalMessage", safeOriginalMessage);
            context.setVariable("replyBody", safeReplyBody);
            context.setVariable("supportEmail", "veezkalif@gmail.com");
            context.setVariable("year", Year.now().getValue());

            htmlContent = templateEngine.process("mail/inquiry-reply", context);
        } catch (Exception e) {
            System.err.println("Failed to render Thymeleaf template 'mail/inquiry-reply': " + e.getMessage());
            // Fallback inline styled HTML to ensure the email is always delivered professionally
            htmlContent = buildFallbackHtml(safeCustomerName, safeSubject, safeOriginalMessage, safeReplyBody);
        }

        // Clean plain-text fallback for non-HTML email clients
        String plainText =
                "Dear " + safeCustomerName + ",\n\n" +
                "Thank you for contacting TravelGO. Here is our response to your inquiry regarding \"" + safeSubject + "\":\n\n" +
                "-------------------------------------------\n" +
                safeReplyBody + "\n" +
                "-------------------------------------------\n\n" +
                "If you have further questions, please don't hesitate to reach out.\n\n" +
                "Best regards,\n" +
                "TravelGO Support & Package Management Team\n" +
                "Email: veezkalif@gmail.com\n";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom("veezkalif@gmail.com", "TravelGO Support");
            helper.setTo(toEmail);
            helper.setSubject("RE: " + safeSubject + " - TravelGO Support");
            helper.setText(plainText, htmlContent);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to dispatch SMTP email to " + toEmail + ": " + e.getMessage(), e);
        }
    }

    /**
     * Backward-compatible overload.
     */
    public void sendReplyEmail(String toEmail, String subject, String replyBody) {
        sendReplyEmail(toEmail, null, subject, null, replyBody);
    }

    /** Sends the released booking details and a secure in-app PDF download link. */
    public void sendTicketReleaseEmail(Booking booking, String downloadLink) {
        String customerName = booking.getUser().getName();
        String destination = booking.getTourPackage().getDestination().getCity() + ", "
                + booking.getTourPackage().getDestination().getCountry();
        String subject = "Your TravelGO tickets are ready - Booking #TG-" + booking.getId();
        String html = "<!DOCTYPE html><html><body style=\"font-family:Arial,sans-serif;background:#f4f7f6;padding:24px;color:#0f172a\">"
                + "<div style=\"max-width:620px;margin:auto;background:#fff;border-radius:14px;overflow:hidden;border:1px solid #dbe7e1\">"
                + "<div style=\"background:#0b1f11;color:#fff;padding:24px\"><h2 style=\"margin:0;color:#34d399\">TravelGO Tickets Released</h2></div>"
                + "<div style=\"padding:24px\"><p>Hello <strong>" + customerName + "</strong>,</p>"
                + "<p>Your visa officer has released the tickets and booking details for your fully paid trip.</p>"
                + "<p><strong>Package:</strong> " + booking.getTourPackage().getName() + "<br>"
                + "<strong>Destination:</strong> " + destination + "<br>"
                + "<strong>Travel date:</strong> " + booking.getTravelDate() + "<br>"
                + "<strong>Travelers:</strong> " + booking.getNumberOfTravelers() + "</p>"
                + "<p><a href=\"" + downloadLink + "\" style=\"display:inline-block;background:#10b981;color:#061208;text-decoration:none;padding:12px 18px;border-radius:8px;font-weight:700\">Download Ticket PDF</a></p>"
                + "<p style=\"color:#64748b;font-size:13px\">Sign in to your TravelGO account if prompted.</p></div></div></body></html>";
        String plain = "Hello " + customerName + ",\n\nYour TravelGO tickets are ready.\nPackage: "
                + booking.getTourPackage().getName() + "\nDestination: " + destination + "\nTravel date: "
                + booking.getTravelDate() + "\nDownload PDF: " + downloadLink;
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            helper.setFrom("veezkalif@gmail.com", "TravelGO Ticketing");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject(subject);
            helper.setText(plain, html);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Ticket email could not be delivered: " + e.getMessage(), e);
        }
    }

    private String buildFallbackHtml(String customerName, String subject, String originalMessage, String replyBody) {
        return "<!DOCTYPE html><html><body style=\"font-family: Arial, sans-serif; background: #0F172A; color: #333; padding: 20px;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; background: #FFF; border-radius: 12px; overflow: hidden;\">" +
                "<div style=\"background: #0F172A; padding: 24px; color: #FFF;\">" +
                "<h2 style=\"margin: 0; color: #2DD4BF;\">TravelGO Support</h2>" +
                "<p style=\"margin: 5px 0 0 0; color: #94A3B8; font-size: 13px;\">RE: " + subject + "</p></div>" +
                "<div style=\"padding: 24px;\">" +
                "<p>Hello <strong>" + customerName + "</strong>,</p>" +
                "<p>Thank you for contacting TravelGO. Here is our official response:</p>" +
                "<div style=\"background: #F0FDFA; border-left: 4px solid #0D9488; padding: 15px; margin: 15px 0; border-radius: 6px;\">" +
                "<p style=\"margin: 0; font-size: 15px; line-height: 1.6; color: #0F172A; white-space: pre-wrap;\">" + replyBody + "</p></div>" +
                "<p style=\"color: #64748B; font-size: 13px;\">Best regards,<br><strong>TravelGO Support Team</strong><br>Email: veezkalif@gmail.com</p>" +
                "</div></div></body></html>";
    }
}
