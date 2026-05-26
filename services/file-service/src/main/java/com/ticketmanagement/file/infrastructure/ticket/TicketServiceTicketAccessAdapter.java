package com.ticketmanagement.file.infrastructure.ticket;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.ticketmanagement.file.application.ForbiddenOperationException;
import com.ticketmanagement.file.application.TicketAccessUnavailableException;
import com.ticketmanagement.file.application.ticket.TicketAccessContext;
import com.ticketmanagement.file.application.ticket.TicketAccessPort;

@Component
@RequiredArgsConstructor
class TicketServiceTicketAccessAdapter implements TicketAccessPort {

    private static final String ACTOR_ID_HEADER = "X-Actor-Id";
    private static final String ACTOR_ROLES_HEADER = "X-Actor-Roles";

    private final RestClient ticketServiceRestClient;

    @Override
    public void assertCanAccessAttachment(UUID ticketId, TicketAccessContext context) {
        try {
            ticketServiceRestClient.get()
                    .uri("/internal/tickets/{ticketId}/attachment-access", ticketId)
                    .headers(headers -> applyIdentityHeaders(headers, context))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            handleTicketAccessFailure(exception);
        } catch (RestClientException exception) {
            throw new TicketAccessUnavailableException();
        }
    }

    private void applyIdentityHeaders(HttpHeaders headers, TicketAccessContext context) {
        if (context.bearerToken() != null && !context.bearerToken().isBlank()) {
            headers.setBearerAuth(context.bearerToken());
            return;
        }
        headers.set(ACTOR_ID_HEADER, context.actorId().toString());
        headers.set(ACTOR_ROLES_HEADER, String.join(",", context.roles()));
    }

    private void handleTicketAccessFailure(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        if (status == 401 || status == 403 || status == 404) {
            throw ForbiddenOperationException.accessDenied();
        }
        throw new TicketAccessUnavailableException();
    }
}
