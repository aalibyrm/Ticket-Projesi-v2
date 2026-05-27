package com.ticketmanagement.notification.infrastructure.email;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateEngineException;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.ticketmanagement.notification.application.EmailTemplateRenderRequest;
import com.ticketmanagement.notification.application.EmailTemplateRendererPort;
import com.ticketmanagement.notification.application.EmailTemplateRenderingException;
import com.ticketmanagement.notification.application.RenderedEmailTemplate;

@Service
class ThymeleafEmailTemplateRendererAdapter implements EmailTemplateRendererPort {

    private final SpringTemplateEngine templateEngine;

    ThymeleafEmailTemplateRendererAdapter(
            @Qualifier("emailTemplateEngine") SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    // Thymeleaf template setini guvenli model ile render eder.
    @Override
    public RenderedEmailTemplate render(EmailTemplateRenderRequest request) {
        try {
            Context context = new Context(Locale.ROOT, request.model());
            String templateName = request.templateKey().getValue();
            return new RenderedEmailTemplate(
                    normalizeSubject(templateEngine.process("subject/" + templateName, context)),
                    templateEngine.process("text/" + templateName, context),
                    templateEngine.process("html/" + templateName, context));
        } catch (TemplateEngineException exception) {
            throw new EmailTemplateRenderingException("Email template rendering failed", exception);
        }
    }

    // E-posta subject'ini header injection riskine karsi tek satira indirger.
    private static String normalizeSubject(String subject) {
        return subject.replaceAll("\\R+", " ").trim();
    }
}
