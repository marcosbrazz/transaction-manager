package com.wex.transaction.controllers;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wex.transaction.models.Id;
import com.wex.transaction.models.Purchase;
import com.wex.transaction.services.PurchaseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/purchase")
public class PurchaseController {

    private static final Logger log = LoggerFactory.getLogger(PurchaseController.class);
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService service) {
        this.purchaseService = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Purchase purchase) {
        log.info("Received request: {}", purchase.getDescription());
        String id = purchaseService.save(purchase);
        log.debug("Generated ID {} for purchase {}", id, purchase.getDescription());
        return ResponseEntity.ok(new Id(id));
    }

    @GetMapping
    public ResponseEntity<Optional<Purchase>> getByIdAndCurrency(
            @PathVariable("id") String id,
            @RequestParam(value = "currency", required = false, defaultValue = "USD") String currency) {
        
        Optional<Purchase> purchase = purchaseService.getPurchaseByIdAndCurrency(id, currency);

        if (purchase.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // TODO integrate currency conversion service
        return ResponseEntity.ok(
            purchase
        );
    }
}
