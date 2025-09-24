package com.wex.transaction.exceptions;

import java.util.Map;

public class InternalApplicationError extends ApplicationException {
    public InternalApplicationError(String message) {
        super(message);
    }

    public InternalApplicationError(String message, Throwable cause) {
        super(message, cause);
    }
    
    @Override
    public Map<String, String> getAttributes() {
        return Map.of("message", this.getMessage());
    }    
}
