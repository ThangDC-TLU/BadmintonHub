package com.badmintonhub.notiservice.service;

import com.badmintonhub.notiservice.dto.event.OrderPlacedEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateRenderer renderer;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendOrderEmail(@NonNull OrderPlacedEvent event) {
        if (event.getCustomerEmail() == null || event.getCustomerEmail().isBlank()) {
            throw new IllegalArgumentException("customerEmail is required");
        }

        final String subject = "🛒 Đơn hàng #" +
                (event.getOrderCode() != null ? event.getOrderCode() : event.getEventId()) +
                " đã được tạo";

        // Render từ FreeMarker template
        final String html = renderer.renderOrderPlacedEmail(event);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromAddress);
            helper.setTo(event.getCustomerEmail());
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("📧 Sent order email to {}", event.getCustomerEmail());
        } catch (MessagingException e) {
            log.error("❌ Send email failed: {}", e.getMessage(), e);
            throw new RuntimeException("Send order email failed", e);
        }
    }
}