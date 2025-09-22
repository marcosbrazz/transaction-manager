package com.wex.transaction.repository;

import com.wex.transaction.models.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, String> {
    
}
