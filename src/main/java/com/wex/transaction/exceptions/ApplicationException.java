package com.wex.transaction.exceptions;

import java.util.Map;

public abstract class ApplicationException extends RuntimeException {
    
    public ApplicationException() {
        super();
    }

    public ApplicationException(String message) {
        super(message);
    }
    
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract Map<String, String> getAttributes();

}
