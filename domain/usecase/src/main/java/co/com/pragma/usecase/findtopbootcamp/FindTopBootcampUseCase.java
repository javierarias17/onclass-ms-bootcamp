package co.com.pragma.usecase.findtopbootcamp;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.exceptions.BootcampNotFoundException;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.bootcamp.gateways.CapabilityGateway;
import co.com.pragma.model.bootcamp.gateways.PersonGateway;
import co.com.pragma.model.bootcamp.query.CapabilitySummary;
import co.com.pragma.model.bootcamp.query.TechnologySummary;
import co.com.pragma.model.bootcamp.query.TopBootcampEnrollmentSummary;
import co.com.pragma.model.bootcamp.query.TopBootcampResult;
import co.com.pragma.model.common.FieldConstants;
import co.com.pragma.model.exceptions.constant.FunctionalMessageConstants;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FindTopBootcampUseCase {

    private final PersonGateway personGateway;
    private final BootcampRepository bootcampRepository;
    private final CapabilityGateway capabilityGateway;

    public Mono<TopBootcampResult> execute() {
        return personGateway.findTopBootcampEnrollment()
                .flatMap(this::buildResult);
    }

    private Mono<TopBootcampResult> buildResult(TopBootcampEnrollmentSummary topBootcampEnrollment) {
        return bootcampRepository.findById(topBootcampEnrollment.bootcampId())
                .switchIfEmpty(Mono.error(new BootcampNotFoundException(
                        FunctionalMessageConstants.BUSINESS_VALIDATION_FAILED,
                        Map.of(FieldConstants.ID, FunctionalMessageConstants.BOOTCAMP_NOT_FOUND))))
                .flatMap(bootcamp -> capabilityGateway.findCapabilitiesByBootcampIds(List.of(bootcamp.getId()))
                        .map(capabilitiesByBootcamp -> capabilitiesByBootcamp.getOrDefault(bootcamp.getId(), List.of()))
                        .map(capabilities -> toTopBootcampResult(bootcamp, capabilities, topBootcampEnrollment)));
    }

    private TopBootcampResult toTopBootcampResult(Bootcamp bootcamp, List<CapabilitySummary> capabilities,
            TopBootcampEnrollmentSummary topBootcampEnrollment) {
        List<TechnologySummary> technologies = capabilities.stream()
                .flatMap(capability -> capability.technologies().stream())
                .distinct()
                .toList();

        return new TopBootcampResult(bootcamp, capabilities, technologies,
                topBootcampEnrollment.enrolledPersonCount(), topBootcampEnrollment.persons());
    }
}
