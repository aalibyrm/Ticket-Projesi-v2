package com.ticketmanagement.notification.application;

public interface EmailSenderPort {

    void send(EmailMessage message);
}
