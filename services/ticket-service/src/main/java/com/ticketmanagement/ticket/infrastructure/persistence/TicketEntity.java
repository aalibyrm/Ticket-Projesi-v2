package com.ticketmanagement.ticket.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ticketmanagement.ticket.domain.TicketPriority;
import com.ticketmanagement.ticket.domain.TicketStatus;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tickets", schema = "ticket_schema")
public class TicketEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 32)
    private String ticketNumber;

    @Column(nullable = false, updatable = false)
    private UUID customerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private TicketTopicEntity topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routed_department_id")
    private DepartmentEntity routedDepartment;

    @Column(nullable = false, length = 180)
    private String summary;

    @Column(nullable = false, length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketStatus status;

    private UUID assigneeId;

    private UUID assignedTeamId;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public static TicketEntity open(
            UUID id,
            String ticketNumber,
            UUID customerId,
            ProductEntity product,
            TicketTopicEntity topic,
            DepartmentEntity routedDepartment,
            UUID assignedTeamId,
            String summary,
            String description,
            TicketPriority priority) {
        TicketEntity ticket = new TicketEntity();
        ticket.setId(id);
        ticket.setTicketNumber(ticketNumber);
        ticket.setCustomerId(customerId);
        ticket.setProduct(product);
        ticket.setTopic(topic);
        ticket.setRoutedDepartment(routedDepartment);
        ticket.setAssignedTeamId(assignedTeamId);
        ticket.setSummary(summary);
        ticket.setDescription(description);
        ticket.setPriority(priority);
        ticket.setStatus(TicketStatus.NEW);
        return ticket;
    }

    public TicketStatus changeStatus(TicketStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        TicketStatus previousStatus = status;
        status = newStatus;
        return previousStatus;
    }

    public void assignTo(UUID assigneeId, UUID assignedTeamId) {
        this.assigneeId = Objects.requireNonNull(assigneeId, "assigneeId must not be null");
        this.assignedTeamId = assignedTeamId;
    }

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
