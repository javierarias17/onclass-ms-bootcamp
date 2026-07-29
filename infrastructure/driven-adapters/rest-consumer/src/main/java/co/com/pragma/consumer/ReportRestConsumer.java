package co.com.pragma.consumer;

import co.com.pragma.consumer.config.RestConsumerConfig;
import co.com.pragma.consumer.dto.BootcampReportInDto;
import co.com.pragma.model.bootcamp.gateways.ReportGateway;
import co.com.pragma.model.bootcamp.query.BootcampReportData;
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
public class ReportRestConsumer implements ReportGateway {

    private static final String BOOTCAMP_REPORTS_PATH = "/api/v1/bootcamp-reports";
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final Duration RETRY_BACKOFF = Duration.ofMillis(200);

    @Qualifier(RestConsumerConfig.REPORT_WEB_CLIENT)
    private final WebClient client;

    @Override
    @CircuitBreaker(name = "registerBootcampReport")
    public Mono<Void> registerBootcampReport(BootcampReportData reportData) {
        return client.post()
                .uri(BOOTCAMP_REPORTS_PATH)
                .bodyValue(toInDto(reportData))
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(transientErrorRetry());
    }

    private BootcampReportInDto toInDto(BootcampReportData reportData) {
        return new BootcampReportInDto(reportData.bootcampId(), reportData.name(), reportData.description(),
                reportData.launchDate(), reportData.durationInWeeks(), reportData.capabilityCount(),
                reportData.technologyCount(), reportData.enrolledPersonCount());
    }

    private Retry transientErrorRetry() {
        return Retry.backoff(MAX_RETRY_ATTEMPTS, RETRY_BACKOFF)
                .filter(ReportRestConsumer::isTransientError);
    }

    private static boolean isTransientError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException)
            return responseException.getStatusCode().is5xxServerError();
        return throwable instanceof WebClientRequestException;
    }
}
