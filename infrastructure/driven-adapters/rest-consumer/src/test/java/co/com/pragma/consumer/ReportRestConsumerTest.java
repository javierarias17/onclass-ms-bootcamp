package co.com.pragma.consumer;

import co.com.pragma.model.bootcamp.query.BootcampReportData;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportRestConsumerTest {

    private static final Long BOOTCAMP_ID = 10L;
    private static final BootcampReportData REPORT_DATA = new BootcampReportData(BOOTCAMP_ID,
            "Java Backend Bootcamp", "Bootcamp de backend con Java", LocalDate.of(2026, 8, 1), 12, 2, 3, 0);

    private static ReportRestConsumer reportRestConsumer;

    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        reportRestConsumer = new ReportRestConsumer(webClient);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void When_ReportIsAccepted_Expect_CompletionWithoutError() throws InterruptedException {
        // Arrange
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.CREATED.value()));

        // Act & Assert
        StepVerifier.create(reportRestConsumer.registerBootcampReport(REPORT_DATA))
                .verifyComplete();

        RecordedRequest request = mockBackEnd.takeRequest();
        assertTrue(request.getPath().endsWith("/api/v1/bootcamp-reports"));
        assertTrue(request.getBody().readUtf8().contains("\"bootcampId\":10"));
    }

    @Test
    void When_ServerRespondsWithTransientServerError_Expect_RetryUntilSuccess() {
        // Arrange: primer intento falla con 500, segundo intento (reintento) responde bien
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.CREATED.value()));

        // Act & Assert
        StepVerifier.create(reportRestConsumer.registerBootcampReport(REPORT_DATA))
                .verifyComplete();
    }

    @Test
    void When_ServerRespondsWithBusinessError_Expect_NoRetryAndErrorPropagated() {
        // Arrange: solo se encola UNA respuesta 400; si el consumer reintentara,
        // la segunda llamada se quedaría esperando una respuesta que no existe y el test fallaría por timeout.
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()));

        // Act & Assert
        StepVerifier.create(reportRestConsumer.registerBootcampReport(REPORT_DATA))
                .expectError()
                .verify(Duration.ofSeconds(2));
    }
}
