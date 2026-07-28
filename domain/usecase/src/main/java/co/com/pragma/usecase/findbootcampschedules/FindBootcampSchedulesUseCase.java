package co.com.pragma.usecase.findbootcampschedules;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.common.FieldConstants;
import co.com.pragma.model.common.ValidationMessageConstants;
import co.com.pragma.model.common.validator.FieldValidator;
import co.com.pragma.model.exceptions.FieldsValidationException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class FindBootcampSchedulesUseCase {

    private final BootcampRepository bootcampRepository;

    public Mono<List<Bootcamp>> execute(List<Long> bootcampIds) {
        Map<String, String> errors = collectFieldFormatErrors(bootcampIds);

        if (!errors.isEmpty())
            return Mono.error(new FieldsValidationException(errors));

        return bootcampRepository.findByIds(bootcampIds).collectList();
    }

    private Map<String, String> collectFieldFormatErrors(List<Long> bootcampIds) {
        Map<String, String> errors = new LinkedHashMap<>();
        FieldValidator.validateNotEmpty(bootcampIds, FieldConstants.BOOTCAMP_IDS,
                ValidationMessageConstants.MSG_BOOTCAMP_IDS_REQUIRED, errors);
        return errors;
    }
}
