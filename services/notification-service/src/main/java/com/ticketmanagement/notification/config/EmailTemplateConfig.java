package com.ticketmanagement.notification.config;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
class EmailTemplateConfig {

    @Bean("emailTemplateEngine")
    SpringTemplateEngine emailTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlEmailTemplateResolver());
        templateEngine.addTemplateResolver(textEmailTemplateResolver());
        return templateEngine;
    }

    private static ClassLoaderTemplateResolver htmlEmailTemplateResolver() {
        ClassLoaderTemplateResolver resolver = baseResolver(1, TemplateMode.HTML, ".html");
        resolver.setResolvablePatterns(Set.of("html/*"));
        return resolver;
    }

    private static ClassLoaderTemplateResolver textEmailTemplateResolver() {
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
        resolver.setCacheable(true);
        resolver.setOrder(order);
        return resolver;
    }
}
