package com.ticketmanagement.file.infrastructure.web;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Correlation-Id";
    private static final String CONTEXT_KEY = "correlationId";
    private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._:-]{1,160}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request.getHeader(HEADER_NAME));
        ThreadContext.put(CONTEXT_KEY, correlationId);
        response.setHeader(HEADER_NAME, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            ThreadContext.remove(CONTEXT_KEY);
        }
    }

    public static String currentCorrelationId() {
        String correlationId = ThreadContext.get(CONTEXT_KEY);
        return correlationId == null || correlationId.isBlank() ? "unknown" : correlationId;
    }

    private static String resolveCorrelationId(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return UUID.randomUUID().toString();
        }
        String candidate = headerValue.trim();
        if (!SAFE_CORRELATION_ID.matcher(candidate).matches()) {
            return UUID.randomUUID().toString();
        }
        return candidate;
    }
}
