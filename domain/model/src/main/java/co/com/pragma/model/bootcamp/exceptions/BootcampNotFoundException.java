package co.com.pragma.model.bootcamp.exceptions;

import co.com.pragma.model.exceptions.FunctionalException;

import java.util.Map;

public class BootcampNotFoundException extends FunctionalException {
    public BootcampNotFoundException(String message, Map<String, String> errors) {
        super(message, errors);
    }
}
