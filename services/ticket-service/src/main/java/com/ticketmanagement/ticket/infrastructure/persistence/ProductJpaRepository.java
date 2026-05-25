package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    List<ProductEntity> findByActiveTrueOrderByNameAsc();

    Optional<ProductEntity> findByIdAndActiveTrue(UUID id);
}

