package com.wex.transaction.controllers;

import com.wex.transaction.models.Purchase;
import com.wex.transaction.services.PurchaseService;
import com.wex.transaction.exceptions.ServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseController.class)
@Import(PurchaseControllerTest.TestConfig.class)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseService purchaseService;

    static class TestConfig {
        @Bean
        PurchaseService purchaseService() {
            return Mockito.mock(PurchaseService.class);
        }
    }

    @Test
    void shouldReturnIdWhenPurchaseCreated() throws Exception {
        when(purchaseService.save(any(Purchase.class))).thenReturn("abc123def456gh78");

        mockMvc.perform(post("/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Test purchase",
                                  "transactionDate": "2025-09-18",
                                  "amount": 100.50
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("abc123def456gh78"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceFails() throws Exception {
        when(purchaseService.save(any(Purchase.class)))
                .thenThrow(new ServiceException("Some error", null));

        mockMvc.perform(post("/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Test purchase",
                                  "transactionDate": "2025-09-18",
                                  "amount": 100.50
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors[0].message").value("Some error"));
    }
}
