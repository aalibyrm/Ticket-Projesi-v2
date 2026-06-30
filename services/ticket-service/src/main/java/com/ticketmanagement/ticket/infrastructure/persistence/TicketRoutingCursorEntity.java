package com.ticketmanagement.ticket.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ticket_routing_cursors", schema = "ticket_schema")
public class TicketRoutingCursorEntity {

    @Id
    @Column(name = "topic_id", nullable = false, updatable = false)
    private UUID topicId;

    @Column(name = "next_route_index", nullable = false)
    private int nextRouteIndex;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public static TicketRoutingCursorEntity create(UUID topicId) {
        TicketRoutingCursorEntity cursor = new TicketRoutingCursorEntity();
        cursor.topicId = topicId;
        cursor.nextRouteIndex = 0;
        cursor.createdAt = OffsetDateTime.now();
        cursor.updatedAt = cursor.createdAt;
        return cursor;
    }

    public int currentIndex(int routeCount) {
        if (routeCount <= 0) {
            return 0;
        }
        return Math.floorMod(nextRouteIndex, routeCount);
    }

    public void advance(int routeCount) {
        if (routeCount <= 0) {
            nextRouteIndex = 0;
        } else {
            nextRouteIndex = Math.floorMod(nextRouteIndex + 1, routeCount);
        }
        updatedAt = OffsetDateTime.now();
    }
}
