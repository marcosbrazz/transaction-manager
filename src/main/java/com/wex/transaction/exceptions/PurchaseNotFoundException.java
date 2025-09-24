package com.wex.transaction.exceptions;

import java.util.Map;

public class PurchaseNotFoundException extends ApplicationException {

    String purchaseId;

    public PurchaseNotFoundException(String purchaseId) {
        super();
        this.purchaseId = purchaseId;
    }

    @Override
    public Map<String, String> getAttributes() {
        return Map.of("message", "Purchase " + purchaseId + " does not exists");
    }
    
}
