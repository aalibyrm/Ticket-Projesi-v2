package com.ticketmanagement.notification.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.ticketmanagement.notification.application.EmailDeliveryException;
import com.ticketmanagement.notification.application.EmailMessage;
import com.ticketmanagement.notification.config.EmailSenderProperties;

class SmtpEmailSenderAdapterTests {

    @Test
    void sendsMimeMessageWithConfiguredSenderAndRecipient() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        EmailSenderProperties properties = senderProperties();
        SmtpEmailSenderAdapter adapter = new SmtpEmailSenderAdapter(mailSender, properties);

        adapter.send(new EmailMessage(
                "customer@example.com",
                "Ticket created",
                "Ticket TCK-6001 was created.",
                "<p>Ticket TCK-6001 was created.</p>"));

        verify(mailSender).send(same(mimeMessage));
        assertThat(mimeMessage.getFrom()[0].toString())
                .contains("Ticket Management")
                .contains("no-reply@ticket.local");
        assertThat(mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString())
                .isEqualTo("customer@example.com");
        assertThat(mimeMessage.getSubject()).isEqualTo("Ticket created");
    }

    @Test
    void rejectsInvalidRecipientBeforeSmtpSend() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        SmtpEmailSenderAdapter adapter = new SmtpEmailSenderAdapter(mailSender, senderProperties());

        assertThatThrownBy(() -> adapter.send(new EmailMessage(
                "not-an-email",
                "Ticket created",
                "Ticket was created.",
                null)))
                .isInstanceOf(EmailDeliveryException.class)
                .hasMessage("Email send failed");
        verify(mailSender, never()).send(same(mimeMessage));
    }

    private static EmailSenderProperties senderProperties() {
        EmailSenderProperties properties = new EmailSenderProperties();
        properties.setFromAddress("no-reply@ticket.local");
        properties.setFromName("Ticket Management");
        return properties;
    }
}
