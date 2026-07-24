package co.com.pragma.model.bootcamp.valueobject;

import co.com.pragma.model.common.FieldConstants;
import co.com.pragma.model.common.ValidationMessageConstants;
import co.com.pragma.model.common.validator.FieldValidator;
import co.com.pragma.model.exceptions.FieldsValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

public record BootcampDurationInWeeks(Integer value) {

    public BootcampDurationInWeeks {
        Map<String, String> errors = new LinkedHashMap<>();
        validate(value, errors);
        if (!errors.isEmpty())
            throw new FieldsValidationException(errors);
    }

    public static void validate(Integer value, Map<String, String> errors) {
        FieldValidator.validateNotNull(value, FieldConstants.DURATION_IN_WEEKS,
                ValidationMessageConstants.MSG_DURATION_IN_WEEKS_REQUIRED, errors);
        if (!errors.containsKey(FieldConstants.DURATION_IN_WEEKS))
            FieldValidator.validatePositive(value, FieldConstants.DURATION_IN_WEEKS,
                    ValidationMessageConstants.MSG_DURATION_IN_WEEKS_MUST_BE_POSITIVE, errors);
    }
}
