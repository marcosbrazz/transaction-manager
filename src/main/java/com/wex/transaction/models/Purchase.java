package com.wex.transaction.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "purchases")
public class Purchase {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 50, message = "Description must not exceed 50 characters")
    private String description;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;

    @NotNull(message = "Purchase amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Purchase amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Purchase amount must have at most 2 decimal places")
    private BigDecimal amount;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal purchaseAmount) {
        if (purchaseAmount != null) {
            this.amount = purchaseAmount.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.amount = null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
