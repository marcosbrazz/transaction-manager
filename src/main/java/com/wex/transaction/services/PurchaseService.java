package com.wex.transaction.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.wex.transaction.exceptions.DuplicatePurchaseException;
import com.wex.transaction.exceptions.ServiceException;
import com.wex.transaction.models.Purchase;
import com.wex.transaction.repository.PurchaseRepository;

@Service
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);
    private final PurchaseRepository repository;

    public PurchaseService(PurchaseRepository repository) {
        this.repository = repository;
    }

    public String save(Purchase purchase) throws ServiceException {
        final String id = this.generateId(purchase);
        purchase.setId(id);
        if (this.repository.existsById(id)) {
            log.warn("Duplicated purchase with Id {}", id);
            throw new DuplicatePurchaseException(id);
        }
        this.repository.save(purchase);
        log.info("Purchase {} saved with Id {}", purchase.getDescription(), id);
        return id;
    }

    private String generateId(Purchase purchase) throws ServiceException {
        try {
            String rawData = purchase.getDescription()
                        + purchase.getTransactionDate()
                        + purchase.getAmount();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawData.getBytes(StandardCharsets.UTF_8));
            String url_safe_hash = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return url_safe_hash.substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating Id", e);
            throw new ServiceException("Failed to generate id", e);
        }
    }
}