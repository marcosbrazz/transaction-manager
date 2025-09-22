package com.wex.transaction.services;

import com.wex.transaction.exceptions.DuplicatePurchaseException;
import com.wex.transaction.exceptions.ServiceException;
import com.wex.transaction.models.Purchase;
import com.wex.transaction.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseServiceTest {

    private PurchaseRepository repository;
    private PurchaseService service;

    @BeforeEach
    void setUp() {
        repository = mock(PurchaseRepository.class);
        service = new PurchaseService(repository);
    }

    @Test
    void shouldSavePurchaseAndReturnId() throws ServiceException {
        Purchase purchase = new Purchase();
        purchase.setDescription("Test purchase");
        purchase.setTransactionDate(LocalDate.of(2025, 9, 18));
        purchase.setAmount(new BigDecimal("123.45"));

        when(repository.existsById(anyString())).thenReturn(false);

        String id = service.save(purchase);

        assertNotNull(id);
        assertEquals(id, purchase.getId());
        verify(repository).save(purchase);
    }

    @Test
    void shouldThrowDuplicatePurchaseExceptionWhenIdExists() {
        Purchase purchase = new Purchase();
        purchase.setDescription("Duplicate purchase");
        purchase.setTransactionDate(LocalDate.of(2025, 9, 18));
        purchase.setAmount(new BigDecimal("123.45"));

        when(repository.existsById(anyString())).thenReturn(true);

        assertThrows(DuplicatePurchaseException.class, () -> service.save(purchase));
        verify(repository, never()).save(any(Purchase.class));
    }

    @Test
    void shouldThrowPurchaseServiceExceptionWhenAlgorithmNotAvailable() {
        Purchase purchase = new Purchase();
        purchase.setDescription("Fail purchase");
        purchase.setTransactionDate(LocalDate.of(2025, 9, 18));
        purchase.setAmount(new BigDecimal("123.45"));

        try (var mocked = Mockito.mockStatic(java.security.MessageDigest.class)) {
            mocked.when(() -> java.security.MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new java.security.NoSuchAlgorithmException("SHA-256 not available"));

            ServiceException ex =
                    assertThrows(ServiceException.class, () -> service.save(purchase));

            assertTrue(ex.getMessage().contains("Failed to generate id"));
        }
    }
}
