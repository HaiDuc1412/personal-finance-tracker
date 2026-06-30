package com.haiduc.personalfinancetracker.budget;

import com.haiduc.personalfinancetracker.budget.event.BudgetAlertEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JavaMailSenderEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder templateBuilder;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Override
    public void sendBudgetAlert(BudgetAlertEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(event.getUserEmail());
            helper.setSubject(templateBuilder.buildSubject(event));
            helper.setText(templateBuilder.buildHtmlBody(event), true);

            mailSender.send(message);

            log.info("Budget alert email sent: to={}, type={}, category={}",
                    event.getUserEmail(), event.getAlertType(), event.getCategoryName());

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send budget alert email to {}: {}", event.getUserEmail(), e.getMessage());
        }
    }
}
