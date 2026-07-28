package co.com.pragma.usecase.deletebootcamp;

import java.util.LinkedHashMap;
import java.util.Map;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.BootcampStatusEnum;
import co.com.pragma.model.bootcamp.exceptions.BootcampNotFoundException;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.bootcamp.gateways.CapabilityGateway;
import co.com.pragma.model.common.FieldConstants;
import co.com.pragma.model.common.ValidationMessageConstants;
import co.com.pragma.model.common.validator.FieldValidator;
import co.com.pragma.model.exceptions.FieldsValidationException;
import co.com.pragma.model.exceptions.constant.FunctionalMessageConstants;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteBootcampUseCase {

    private final BootcampRepository bootcampRepository;
    private final CapabilityGateway capabilityGateway;

    public Mono<Void> execute(String id) {
        Map<String, String> errors = collectFieldFormatErrors(id);

        if (!errors.isEmpty())
            return Mono.error(new FieldsValidationException(errors));

        return bootcampRepository.findById(Long.valueOf(id))
                .switchIfEmpty(Mono.error(new BootcampNotFoundException(
                        FunctionalMessageConstants.BUSINESS_VALIDATION_FAILED,
                        Map.of(FieldConstants.ID, FunctionalMessageConstants.BOOTCAMP_NOT_FOUND))))
                .flatMap(bootcamp -> bootcamp.getStatus() == BootcampStatusEnum.DELETING
                        ? resumeCascade(bootcamp)
                        : markDeleting(bootcamp).flatMap(this::resumeCascade));
    }

    // El job de reintento reutiliza esta misma orquestación
    public Mono<Void> resumeCascade(Bootcamp bootcamp) {
        return capabilityGateway.deleteOrphanedCapabilitiesForBootcamp(bootcamp.getId())
                .then(Mono.defer(() -> bootcampRepository.deleteById(bootcamp.getId())));
    }

    private Mono<Bootcamp> markDeleting(Bootcamp bootcamp) {
        return bootcampRepository.updateStatus(Bootcamp.builder()
                .id(bootcamp.getId())
                .version(bootcamp.getVersion())
                .name(bootcamp.getName().value())
                .description(bootcamp.getDescription().value())
                .launchDate(bootcamp.getLaunchDate().value())
                .durationInWeeks(bootcamp.getDurationInWeeks().value())
                .status(BootcampStatusEnum.DELETING)
                .capabilityCount(bootcamp.getCapabilityCount())
                .build());
    }

    private Map<String, String> collectFieldFormatErrors(String id) {
        Map<String, String> errors = new LinkedHashMap<>();
        FieldValidator.validateNotBlank(id, FieldConstants.ID,
                ValidationMessageConstants.MSG_ID_REQUIRED, errors);
        FieldValidator.validateNumericFormat(id, FieldConstants.ID,
                ValidationMessageConstants.MSG_ID_MUST_BE_NUMERIC, errors);
        return errors;
    }
}
