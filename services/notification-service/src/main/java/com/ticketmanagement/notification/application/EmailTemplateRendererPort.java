package com.ticketmanagement.notification.application;

public interface EmailTemplateRendererPort {

    // Secilen transactional e-posta template'ini subject, text ve HTML govdesine cevirir.
    RenderedEmailTemplate render(EmailTemplateRenderRequest request);
}
