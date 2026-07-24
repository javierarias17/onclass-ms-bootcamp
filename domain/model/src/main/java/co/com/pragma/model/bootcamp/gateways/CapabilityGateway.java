package co.com.pragma.model.bootcamp.gateways;

import reactor.core.publisher.Mono;

import java.util.List;

public interface CapabilityGateway {

    Mono<List<Long>> checkCapabilitiesExistence(List<Long> capabilityIds);

    Mono<Void> linkBootcampCapabilities(Long bootcampId, List<Long> capabilityIds);

    Mono<Void> deleteBootcampCapabilities(Long bootcampId);
}
