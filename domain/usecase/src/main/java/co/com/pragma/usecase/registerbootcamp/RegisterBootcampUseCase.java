package co.com.pragma.usecase.registerbootcamp;

import java.util.LinkedHashMap;
import java.util.Map;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.BootcampStatusEnum;
import co.com.pragma.model.bootcamp.command.BootcampCreateCommand;
import co.com.pragma.model.bootcamp.exceptions.BootcampAlreadyExistsException;
import co.com.pragma.model.bootcamp.exceptions.CapabilitiesNotFoundException;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.bootcamp.gateways.CapabilityGateway;
import co.com.pragma.model.bootcamp.valueobject.BootcampCapabilityIds;
import co.com.pragma.model.bootcamp.valueobject.BootcampDescription;
import co.com.pragma.model.bootcamp.valueobject.BootcampDurationInWeeks;
import co.com.pragma.model.bootcamp.valueobject.BootcampLaunchDate;
import co.com.pragma.model.bootcamp.valueobject.BootcampName;
import co.com.pragma.model.common.FieldConstants;
import co.com.pragma.model.exceptions.FieldsValidationException;
import co.com.pragma.model.exceptions.constant.FunctionalMessageConstants;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RegisterBootcampUseCase {

    private static final int NO_CAPABILITIES_LINKED_YET = 0;

    private final BootcampRepository bootcampRepository;
    private final CapabilityGateway capabilityGateway;

    public Mono<Bootcamp> execute(BootcampCreateCommand command) {
        Map<String, String> errors = collectFieldFormatErrors(command);

        if (!errors.isEmpty())
            return Mono.error(new FieldsValidationException(errors));

        return bootcampRepository.findByName(command.name())
                .flatMap(existingBootcamp -> resumeOrReject(existingBootcamp, command))
                .switchIfEmpty(Mono.defer(() -> registerAndLink(null, null, command)));
    }

    private Mono<Bootcamp> resumeOrReject(Bootcamp existingBootcamp, BootcampCreateCommand command) {
        if (existingBootcamp.getStatus() == BootcampStatusEnum.COMPLETE)
            return Mono.error(new BootcampAlreadyExistsException(
                    FunctionalMessageConstants.BUSINESS_VALIDATION_FAILED,
                    Map.of(FieldConstants.NAME, FunctionalMessageConstants.BOOTCAMP_ALREADY_EXISTS)));

        return registerAndLink(existingBootcamp.getId(), existingBootcamp.getVersion(), command);
    }

    private Mono<Bootcamp> registerAndLink(Long bootcampId, Long version, BootcampCreateCommand command) {
        return capabilityGateway.checkCapabilitiesExistence(command.capabilityIds())
                .flatMap(missingIds -> missingIds.isEmpty()
                        ? bootcampRepository.save(Bootcamp.builder()
                                .id(bootcampId)
                                .version(version)
                                .name(command.name())
                                .description(command.description())
                                .launchDate(command.launchDate())
                                .durationInWeeks(command.durationInWeeks())
                                .status(BootcampStatusEnum.PENDING)
                                .capabilityCount(NO_CAPABILITIES_LINKED_YET)
                                .build())
                        : Mono.error(new CapabilitiesNotFoundException(
                                FunctionalMessageConstants.BUSINESS_VALIDATION_FAILED,
                                Map.of(FieldConstants.CAPABILITY_IDS,
                                        String.format(FunctionalMessageConstants.CAPABILITIES_NOT_FOUND, missingIds)))))
                .flatMap(savedBootcamp -> deleteStaleLinksIfResuming(bootcampId, savedBootcamp.getId())
                        .then(Mono.defer(() -> capabilityGateway.linkBootcampCapabilities(savedBootcamp.getId(), command.capabilityIds())))
                        .then(Mono.defer(() -> bootcampRepository.save(Bootcamp.builder()
                                .id(savedBootcamp.getId())
                                .version(savedBootcamp.getVersion())
                                .name(savedBootcamp.getName().value())
                                .description(savedBootcamp.getDescription().value())
                                .launchDate(savedBootcamp.getLaunchDate().value())
                                .durationInWeeks(savedBootcamp.getDurationInWeeks().value())
                                .status(BootcampStatusEnum.COMPLETE)
                                .capabilityCount(command.capabilityIds().size())
                                .build()))));
    }

    private Mono<Void> deleteStaleLinksIfResuming(Long existingBootcampId, Long savedBootcampId) {
        return existingBootcampId != null
                ? capabilityGateway.deleteBootcampCapabilities(savedBootcampId)
                : Mono.empty();
    }

    private Map<String, String> collectFieldFormatErrors(BootcampCreateCommand command) {
        Map<String, String> errors = new LinkedHashMap<>();
        BootcampName.validate(command.name(), errors);
        BootcampDescription.validate(command.description(), errors);
        BootcampLaunchDate.validate(command.launchDate(), errors);
        BootcampDurationInWeeks.validate(command.durationInWeeks(), errors);
        BootcampCapabilityIds.validate(command.capabilityIds(), errors);
        return errors;
    }
}
