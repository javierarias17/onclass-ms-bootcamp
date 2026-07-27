package co.com.pragma.model.bootcamp.gateways;

import co.com.pragma.model.bootcamp.query.CapabilitySummary;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface CapabilityGateway {

    Mono<List<Long>> checkCapabilitiesExistence(List<Long> capabilityIds);

    Mono<Void> linkBootcampCapabilities(Long bootcampId, List<Long> capabilityIds);

    Mono<Void> deleteBootcampCapabilities(Long bootcampId);

    Mono<Map<Long, List<CapabilitySummary>>> findCapabilitiesByBootcampIds(List<Long> bootcampIds);

    Mono<Void> deleteOrphanedCapabilitiesForBootcamp(Long bootcampId);
}
