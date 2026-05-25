package com.ticketmanagement.ticket.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.api.dto.ProductResponse;
import com.ticketmanagement.ticket.infrastructure.persistence.ProductJpaRepository;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductJpaRepository productRepository;

    // Ticket acilis formunda secilebilecek aktif urunleri listeler.
    @Transactional(readOnly = true)
    public List<ProductResponse> listActiveProducts() {
        return productRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(product -> new ProductResponse(product.getId(), product.getCode(), product.getName()))
                .toList();
    }
}

