package co.com.pragma.model.bootcamp.exceptions;

import co.com.pragma.model.exceptions.FunctionalException;

import java.util.Map;

public class CapabilitiesNotFoundException extends FunctionalException {
    public CapabilitiesNotFoundException(String message, Map<String, String> errors) {
        super(message, errors);
    }
}
