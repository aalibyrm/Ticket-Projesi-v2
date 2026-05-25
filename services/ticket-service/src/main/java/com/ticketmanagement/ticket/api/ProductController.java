package com.ticketmanagement.ticket.api;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketmanagement.ticket.api.dto.ProductResponse;
import com.ticketmanagement.ticket.application.ProductQueryService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
class ProductController {

    private final ProductQueryService productQueryService;

    // Ticket acarken kullanilacak aktif urun dropdown verisini dondurur.
    @GetMapping
    List<ProductResponse> listProducts() {
        return productQueryService.listActiveProducts();
    }
}

