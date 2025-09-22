package com.wex.transaction.exceptions;

import java.util.Map;

public class ServiceException extends ApplicationException {

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    @Override
    public Map<String, String> getAttributes() {
        return Map.of("message", this.getMessage());
    }

    
}