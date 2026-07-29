package co.com.pragma.consumer;

import co.com.pragma.consumer.config.RestConsumerConfig;
import co.com.pragma.consumer.dto.EnrolledPersonDto;
import co.com.pragma.consumer.dto.TopBootcampEnrollmentOutDto;
import co.com.pragma.model.bootcamp.exceptions.ServiceUnavailableException;
import co.com.pragma.model.bootcamp.gateways.PersonGateway;
import co.com.pragma.model.bootcamp.query.EnrolledPersonSummary;
import co.com.pragma.model.bootcamp.query.TopBootcampEnrollmentSummary;
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

@Service
@RequiredArgsConstructor
public class PersonRestConsumer implements PersonGateway {

    private static final String TOP_BOOTCAMP_PATH = "/api/v1/bootcamp-persons/top-bootcamp";
    private static final String SERVICE_CALL_FAILED_MESSAGE = "Unable to reach person service at %s";
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final Duration RETRY_BACKOFF = Duration.ofMillis(200);

    @Qualifier(RestConsumerConfig.PERSON_WEB_CLIENT)
    private final WebClient client;

    @Override
    @CircuitBreaker(name = "findTopBootcampEnrollment")
    public Mono<TopBootcampEnrollmentSummary> findTopBootcampEnrollment() {
        return client.get()
                .uri(TOP_BOOTCAMP_PATH)
                .retrieve()
                .bodyToMono(TopBootcampEnrollmentOutDto.class)
                .map(this::toTopBootcampEnrollmentSummary)
                .retryWhen(transientErrorRetry())
                .onErrorMap(error -> new ServiceUnavailableException(
                        SERVICE_CALL_FAILED_MESSAGE.formatted(TOP_BOOTCAMP_PATH), error));
    }

    private TopBootcampEnrollmentSummary toTopBootcampEnrollmentSummary(TopBootcampEnrollmentOutDto response) {
        return new TopBootcampEnrollmentSummary(response.bootcampId(), response.enrolledPersonCount(),
                response.persons().stream()
                        .map(person -> new EnrolledPersonSummary(person.name(), person.email()))
                        .toList());
    }

    private Retry transientErrorRetry() {
        return Retry.backoff(MAX_RETRY_ATTEMPTS, RETRY_BACKOFF)
                .filter(PersonRestConsumer::isTransientError);
    }

    private static boolean isTransientError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException)
            return responseException.getStatusCode().is5xxServerError();
        return throwable instanceof WebClientRequestException;
    }
}
