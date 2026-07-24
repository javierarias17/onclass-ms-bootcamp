package co.com.pragma.model.bootcamp.valueobject;

import co.com.pragma.model.common.FieldConstants;
import co.com.pragma.model.common.ValidationMessageConstants;
import co.com.pragma.model.common.validator.FieldValidator;
import co.com.pragma.model.exceptions.FieldsValidationException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public record BootcampLaunchDate(LocalDate value) {

    public BootcampLaunchDate {
        Map<String, String> errors = new LinkedHashMap<>();
        validate(value, errors);
        if (!errors.isEmpty())
            throw new FieldsValidationException(errors);
    }

    public static void validate(LocalDate value, Map<String, String> errors) {
        FieldValidator.validateNotNull(value, FieldConstants.LAUNCH_DATE,
                ValidationMessageConstants.MSG_LAUNCH_DATE_REQUIRED, errors);
    }
}
