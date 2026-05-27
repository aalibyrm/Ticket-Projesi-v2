package com.ticketmanagement.notification.infrastructure.email;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ticketmanagement.notification.application.EmailDeliveryException;
import com.ticketmanagement.notification.application.EmailMessage;
import com.ticketmanagement.notification.application.EmailSenderPort;
import com.ticketmanagement.notification.config.EmailSenderProperties;

@Component
@RequiredArgsConstructor
class SmtpEmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;
    private final EmailSenderProperties properties;

    @Override
    public void send(EmailMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    message.htmlBody() != null,
                    StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress());
            helper.setTo(recipientAddress(message.recipientEmail()));
            helper.setSubject(message.subject());
            if (message.htmlBody() == null) {
                helper.setText(message.textBody(), false);
            } else {
                helper.setText(message.textBody(), message.htmlBody());
            }
            mailSender.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException | MailException exception) {
            throw new EmailDeliveryException("Email send failed", exception);
        }
    }

    private InternetAddress fromAddress() throws UnsupportedEncodingException {
        return new InternetAddress(properties.getFromAddress(), properties.getFromName(), StandardCharsets.UTF_8.name());
    }

    private static InternetAddress recipientAddress(String email) throws AddressException {
        InternetAddress address = new InternetAddress(email);
        address.validate();
        return address;
    }
}
