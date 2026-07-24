package co.com.pragma.model.bootcamp.exceptions;

import co.com.pragma.model.exceptions.FunctionalException;

import java.util.Map;

public class BootcampAlreadyExistsException extends FunctionalException {
    public BootcampAlreadyExistsException(String message, Map<String, String> errors) {
        super(message, errors);
    }
}
