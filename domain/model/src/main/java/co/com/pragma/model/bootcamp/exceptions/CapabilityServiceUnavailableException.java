package co.com.pragma.model.bootcamp.exceptions;

import co.com.pragma.model.exceptions.TechnicalException;

public class CapabilityServiceUnavailableException extends TechnicalException {
    public CapabilityServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
