package co.com.pragma.consumer;

import co.com.pragma.consumer.dto.BootcampCapabilityLinkInDto;
import co.com.pragma.consumer.dto.CapabilitiesByBootcampEntryDto;
import co.com.pragma.consumer.dto.CapabilitiesByBootcampInDto;
import co.com.pragma.consumer.dto.CapabilitiesByBootcampOutDto;
import co.com.pragma.consumer.dto.CapabilityExistenceInDto;
import co.com.pragma.consumer.dto.CapabilityExistenceOutDto;
import co.com.pragma.model.bootcamp.exceptions.ServiceUnavailableException;
import co.com.pragma.model.bootcamp.gateways.CapabilityGateway;
import co.com.pragma.consumer.config.RestConsumerConfig;
import co.com.pragma.model.bootcamp.query.CapabilitySummary;
import co.com.pragma.model.bootcamp.query.TechnologySummary;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CapabilityRestConsumer implements CapabilityGateway {

    private static final String EXISTENCE_CHECK_PATH = "/api/v1/capabilities/existence-check";
    private static final String BOOTCAMP_CAPABILITIES_PATH = "/api/v1/bootcamp-capabilities";
    private static final String DELETE_BOOTCAMP_CAPABILITIES_PATH = BOOTCAMP_CAPABILITIES_PATH + "/{bootcampId}";
    private static final String BOOTCAMP_CAPABILITIES_BY_BOOTCAMP_IDS_PATH = BOOTCAMP_CAPABILITIES_PATH + "/by-bootcamp-ids";
    private static final String DELETE_ORPHANED_CAPABILITIES_PATH = BOOTCAMP_CAPABILITIES_PATH + "/{bootcampId}/cascade";

    private static final String SERVICE_CALL_FAILED_MESSAGE = "Unable to reach capability service at %s";
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final Duration RETRY_BACKOFF = Duration.ofMillis(200);

    @Qualifier(RestConsumerConfig.CAPABILITY_WEB_CLIENT)
    private final WebClient client;

    @Override
    @CircuitBreaker(name = "checkCapabilitiesExistence")
    public Mono<List<Long>> checkCapabilitiesExistence(List<Long> capabilityIds) {
        return client.post()
                .uri(EXISTENCE_CHECK_PATH)
                .bodyValue(new CapabilityExistenceInDto(capabilityIds))
                .retrieve()
                .bodyToMono(CapabilityExistenceOutDto.class)
                .map(CapabilityExistenceOutDto::missingIds)
                .retryWhen(transientErrorRetry())
                .onErrorMap(error -> new ServiceUnavailableException(
                        buildServiceCallFailedMessage(EXISTENCE_CHECK_PATH), error));
    }

    @Override
    @CircuitBreaker(name = "linkBootcampCapabilities")
    public Mono<Void> linkBootcampCapabilities(Long bootcampId, List<Long> capabilityIds) {
        return client.post()
                .uri(BOOTCAMP_CAPABILITIES_PATH)
                .bodyValue(new BootcampCapabilityLinkInDto(bootcampId, capabilityIds))
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(transientErrorRetry())
                .onErrorMap(error -> new ServiceUnavailableException(
                        buildServiceCallFailedMessage(BOOTCAMP_CAPABILITIES_PATH), error));
    }

    @Override
    @CircuitBreaker(name = "deleteBootcampCapabilities")
    public Mono<Void> deleteBootcampCapabilities(Long bootcampId) {
        return client.delete()
                .uri(DELETE_BOOTCAMP_CAPABILITIES_PATH, bootcampId)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(transientErrorRetry())
                .onErrorMap(error -> new ServiceUnavailableException(
                        buildServiceCallFailedMessage(DELETE_BOOTCAMP_CAPABILITIES_PATH), error));
    }

    @Override
    @CircuitBreaker(name = "findCapabilitiesByBootcampIds")
    public Mono<Map<Long, List<CapabilitySummary>>> findCapabilitiesByBootcampIds(List<Long> bootcampIds) {
        return client.post()
                .uri(BOOTCAMP_CAPABILITIES_BY_BOOTCAMP_IDS_PATH)
                .bodyValue(new CapabilitiesByBootcampInDto(bootcampIds))
                .retrieve()
                .bodyToMono(CapabilitiesByBootcampOutDto.class)
                .map(this::toCapabilitiesByBootcampMap)
                .retryWhen(transientErrorRetry())
                .onErrorMap(error -> new ServiceUnavailableException(
                        buildServiceCallFailedMessage(BOOTCAMP_CAPABILITIES_BY_BOOTCAMP_IDS_PATH), error));
    }

    @Override
    @CircuitBreaker(name = "deleteOrphanedCapabilitiesForBootcamp")
    public Mono<Void> deleteOrphanedCapabilitiesForBootcamp(Long bootcampId) {
        return client.delete()
                .uri(DELETE_ORPHANED_CAPABILITIES_PATH, bootcampId)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(transientErrorRetry())
                .onErrorMap(error -> new ServiceUnavailableException(
                        buildServiceCallFailedMessage(DELETE_ORPHANED_CAPABILITIES_PATH), error));
    }

    private static String buildServiceCallFailedMessage(String path) {
        return SERVICE_CALL_FAILED_MESSAGE.formatted(path);
    }

    private Map<Long, List<CapabilitySummary>> toCapabilitiesByBootcampMap(CapabilitiesByBootcampOutDto response) {
        return response.bootcamps().stream()
                .collect(Collectors.toMap(
                        CapabilitiesByBootcampEntryDto::bootcampId,
                        entry -> entry.capabilities().stream()
                                .map(capability -> new CapabilitySummary(capability.id(), capability.name(),
                                        capability.technologies().stream()
                                                .map(technology -> new TechnologySummary(technology.id(), technology.name()))
                                                .toList()))
                                .toList()));
    }

    private Retry transientErrorRetry() {
        return Retry.backoff(MAX_RETRY_ATTEMPTS, RETRY_BACKOFF)
                .filter(CapabilityRestConsumer::isTransientError);
    }

    private static boolean isTransientError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException)
            return responseException.getStatusCode().is5xxServerError();
        return throwable instanceof WebClientRequestException;
    }
}
