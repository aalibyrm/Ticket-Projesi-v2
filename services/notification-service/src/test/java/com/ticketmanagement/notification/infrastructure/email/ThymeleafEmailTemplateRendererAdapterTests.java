package com.ticketmanagement.notification.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.ticketmanagement.notification.application.EmailTemplateRenderRequest;
import com.ticketmanagement.notification.application.RenderedEmailTemplate;
import com.ticketmanagement.notification.domain.EmailTemplateKey;

class ThymeleafEmailTemplateRendererAdapterTests {

    private final ThymeleafEmailTemplateRendererAdapter renderer =
            new ThymeleafEmailTemplateRendererAdapter(templateEngine());

    @Test
    void rendersAllTransactionalTemplates() {
        for (EmailTemplateKey templateKey : EmailTemplateKey.values()) {
            RenderedEmailTemplate rendered = renderer.render(new EmailTemplateRenderRequest(
                    templateKey,
                    defaultModel()));

            assertThat(rendered.subject()).isNotBlank();
            assertThat(rendered.textBody()).isNotBlank();
            assertThat(rendered.htmlBody()).contains("<html").doesNotContain("internal-only-note");
        }
    }

    @Test
    void escapesUserContentAndFiltersInternalNotes() {
        Map<String, Object> model = defaultModel();
        model.put("ticketNumber", "TCK-<script>alert(1)</script>");
        model.put("customerName", "<b>Alice</b>");
        model.put("internalNotes", "internal-only-note");
        model.put("ticket", Map.of("internalNotes", "nested-internal-note"));

        EmailTemplateRenderRequest request = new EmailTemplateRenderRequest(
                EmailTemplateKey.TICKET_CREATED,
                model);

        RenderedEmailTemplate rendered = renderer.render(request);

        assertThat(rendered.subject())
                .contains("&lt;script&gt;alert(1)&lt;/script&gt;")
                .doesNotContain("<script>")
                .doesNotContain("internal-only-note");
        assertThat(rendered.textBody())
                .contains("&lt;script&gt;alert(1)&lt;/script&gt;")
                .doesNotContain("<script>")
                .doesNotContain("internal-only-note");
        assertThat(rendered.htmlBody())
                .contains("TCK-&lt;script&gt;alert(1)&lt;/script&gt;")
                .contains("&lt;b&gt;Alice&lt;/b&gt;")
                .doesNotContain("<script>")
                .doesNotContain("<b>Alice</b>")
                .doesNotContain("internal-only-note")
                .doesNotContain("nested-internal-note");
        assertThat(request.model()).doesNotContainKey("internalNotes");
        assertThat(((Map<?, ?>) request.model().get("ticket")).containsKey("internalNotes")).isFalse();
    }

    private static Map<String, Object> defaultModel() {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("customerName", "Customer");
        model.put("recipientName", "Recipient");
        model.put("ticketNumber", "TCK-3001");
        model.put("priority", "HIGH");
        model.put("status", "NEW");
        model.put("previousStatus", "NEW");
        model.put("newStatus", "IN_PROGRESS");
        model.put("assigneeName", "Agent One");
        model.put("assignedTeamName", "Support Team");
        model.put("commentAuthorName", "Customer");
        model.put("commentPreview", "External customer comment");
        model.put("riskReason", "Deadline is close");
        model.put("breachReason", "Deadline passed");
        model.put("slaDeadline", "2026-05-28T12:00:00Z");
        model.put("resolutionSummary", "Issue fixed");
        model.put("resolvedAt", "2026-05-28T13:00:00Z");
        model.put("closedAt", "2026-05-28T14:00:00Z");
        model.put("ticketUrl", "https://app.ticket.local/tickets/TCK-3001");
        model.put("internalNotes", "internal-only-note");
        return model;
    }

    private static SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlResolver());
        templateEngine.addTemplateResolver(textResolver());
        return templateEngine;
    }

    private static ClassLoaderTemplateResolver htmlResolver() {
        ClassLoaderTemplateResolver resolver = baseResolver(1, TemplateMode.HTML, ".html");
        resolver.setResolvablePatterns(Set.of("html/*"));
        return resolver;
    }

    private static ClassLoaderTemplateResolver textResolver() {
        ClassLoaderTemplateResolver resolver = baseResolver(2, TemplateMode.TEXT, ".txt");
        resolver.setResolvablePatterns(Set.of("text/*", "subject/*"));
        return resolver;
    }

    private static ClassLoaderTemplateResolver baseResolver(int order, TemplateMode templateMode, String suffix) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/email/");
        resolver.setSuffix(suffix);
        resolver.setTemplateMode(templateMode);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCheckExistence(true);
        resolver.setCacheable(false);
        resolver.setOrder(order);
        return resolver;
    }
}
