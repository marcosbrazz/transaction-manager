package com.wex.transaction.services;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.wex.transaction.exceptions.DuplicatePurchaseException;
import com.wex.transaction.exceptions.InternalApplicationError;
import com.wex.transaction.exceptions.PurchaseNotFoundException;
import com.wex.transaction.exceptions.ServiceException;
import com.wex.transaction.models.Purchase;
import com.wex.transaction.repository.PurchaseRepository;
import com.wex.transaction.services.ExchangeService.ExchangeRate;

@Service
public class PurchaseService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);
    private final PurchaseRepository repository;
    private final ExchangeService exchangeService;

    public PurchaseService(PurchaseRepository repository, ExchangeService exchangeService) {
        this.repository = repository;
        this.exchangeService = exchangeService;
    }

    public String save(Purchase purchase) {
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

    public Purchase getPurchaseByIdAndCurrency(String id, String currency) {
        log.info("Purchase Id {} requested for currency {}", id, currency);
        Purchase purchase = repository.findById(id)
            .orElseThrow(() -> new PurchaseNotFoundException(id));

        LocalDate minDate = purchase.getTransactionDate().minusMonths(6);

        log.debug("Fetching exchange rates for currency={} transactionDate={} minDate={}",
            currency, purchase.getTransactionDate(), minDate);

        List<ExchangeRate> rates = exchangeService
            .getRates(currency, minDate, purchase.getTransactionDate());

        // Guarantee to pick latest rate <= purchase date
        ExchangeRate selectedRate = rates.stream()
            .filter(r -> !r.recordDate().isAfter(purchase.getTransactionDate()))
            .max(Comparator.comparing(ExchangeRate::recordDate))
            .orElseThrow(() -> new ServiceException(
                "No valid exchange rate within 6 months before purchase date for currency " + currency
            ));

        BigDecimal convertedAmount = purchase.getAmount().multiply(selectedRate.exchangeRate());

        purchase.setCurrency(currency);
        purchase.setRate(selectedRate.exchangeRate());
        purchase.setConvertedAmount(convertedAmount);
        return purchase;
    }

    private String generateId(Purchase purchase) {
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
            throw new InternalApplicationError("Failed to generate id", e);
        }
    }
}