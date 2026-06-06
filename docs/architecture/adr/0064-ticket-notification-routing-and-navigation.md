# ADR-0064: Ticket notification routing and navigation

## Status

Accepted

## Context

Ticket notifications existed as user-scoped UI rows and email delivery records, but
the API response did not expose the related ticket id. The frontend could not
navigate from a notification to the affected ticket without parsing message text.

The `ticket.created` event also notified the customer who created the ticket.
For the support workflow this is not useful: a new ticket must alert the routed
support side. Newly created tickets are routed to a team before they are claimed
by a specific agent.

## Decision

Notification records store nullable `ticket_id`, and the web client uses it for
notification click navigation. Customer users open `/tickets/{ticketId}`; support
users open `/agent/tickets/{ticketId}`.

`ticket.created` events carry the routed team and a support notification
recipient. The first active non-lead team member is selected for local demo
accounts because these are the Keycloak users that can log in as agents. If no
non-lead member exists, the team lead is used as fallback.

`ticket.status-changed` events carry `customerId`. Notification-service sends
the resulting UI notification and email to the customer.

## Consequences

- Notification click behavior no longer depends on message text.
- New-ticket notifications and Mailpit emails go to the support side instead of
  the ticket creator.
- Status changes notify the customer through both UI notifications and email.
- The current implementation keeps one notification recipient per event. If
  multiple team members must receive the same new-ticket notification later, the
  notification uniqueness model must move from `source_event_id` to
  `source_event_id + recipient_id`.
