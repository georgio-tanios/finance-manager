package com.myfinance.finance_manager.exception;

/**
 * Thrown when a requested resource (entity) cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entity, Object id) {
        super(String.format("%s not found with id: %s", entity, id));
    }
}
