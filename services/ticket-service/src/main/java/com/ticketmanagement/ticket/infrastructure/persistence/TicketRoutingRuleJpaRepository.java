package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRoutingRuleJpaRepository extends JpaRepository<TicketRoutingRuleEntity, UUID> {

    @Query("""
            select rule
            from TicketRoutingRuleEntity rule
            join fetch rule.topic topic
            join fetch rule.department department
            join fetch rule.team team
            join fetch team.department teamDepartment
            where topic.code = :topicCode
              and topic.active = true
              and rule.active = true
              and department.active = true
              and team.active = true
              and teamDepartment = department
            order by rule.routingOrder asc, team.name asc, team.id asc
            """)
    List<TicketRoutingRuleEntity> findActiveRoutesForTopicCode(String topicCode);
}
