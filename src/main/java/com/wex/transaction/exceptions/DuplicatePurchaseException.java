package com.wex.transaction.exceptions;

import java.util.Map;

public class DuplicatePurchaseException extends ApplicationException {
    private static final String message = "Purchase already exists.";
    private String id;

    public DuplicatePurchaseException(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Map<String, String> getAttributes() {
        return Map.of("id", this.id, "message", DuplicatePurchaseException.message);
    }
    
}
