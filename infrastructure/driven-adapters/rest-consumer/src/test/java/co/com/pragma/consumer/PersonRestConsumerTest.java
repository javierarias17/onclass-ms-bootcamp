package co.com.pragma.consumer;

import co.com.pragma.model.bootcamp.exceptions.ServiceUnavailableException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonRestConsumerTest {

    private static final Long BOOTCAMP_ID = 10L;
    private static final Long ENROLLED_PERSON_COUNT = 2L;
    private static final Long RETRY_RESPONSE_ENROLLED_PERSON_COUNT = 1L;
    private static final String PERSON_NAME = "Ada Lovelace";
    private static final String PERSON_EMAIL = "ada@mail.com";
    private static final String TOP_BOOTCAMP_PATH = "/api/v1/bootcamp-persons/top-bootcamp";

    private static PersonRestConsumer personRestConsumer;

    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        personRestConsumer = new PersonRestConsumer(webClient);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void When_TopBootcampExists_Expect_TopBootcampEnrollmentSummaryReturned() throws InterruptedException {
        // Arrange
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                          "bootcampId": %d,
                          "enrolledPersonCount": %d,
                          "persons": [
                            { "name": "%s", "email": "%s" }
                          ]
                        }
                        """.formatted(BOOTCAMP_ID, ENROLLED_PERSON_COUNT, PERSON_NAME, PERSON_EMAIL)));

        // Act & Assert
        StepVerifier.create(personRestConsumer.findTopBootcampEnrollment())
                .expectNextMatches(result -> result.bootcampId().equals(BOOTCAMP_ID)
                        && result.enrolledPersonCount().equals(ENROLLED_PERSON_COUNT)
                        && result.persons().size() == 1
                        && result.persons().get(0).name().equals(PERSON_NAME))
                .verifyComplete();

        RecordedRequest request = mockBackEnd.takeRequest();
        assertTrue(request.getPath().endsWith(TOP_BOOTCAMP_PATH));
    }

    @Test
    void When_NoEnrollmentsExist_Expect_EmptyMono() {
        // Arrange: person-ms responde 204 (nunca 404) cuando no hay inscripciones, para no confundir esa
        // respuesta de negocio con un 404 real de infraestructura (ruta inexistente, error de configuración, etc.).
        // El consumer no necesita ningún manejo especial: bodyToMono completa vacío por sí solo ante un 204.
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value()));

        // Act & Assert
        StepVerifier.create(personRestConsumer.findTopBootcampEnrollment())
                .verifyComplete();
    }

    @Test
    void When_ServerRespondsWithRealNotFound_Expect_ServiceUnavailableExceptionWithoutRetry() {
        // Arrange: solo se encola UNA respuesta 404; si el consumer reintentara,
        // la segunda llamada se quedaría esperando una respuesta que no existe y el test fallaría por timeout.
        // Ahora un 404 SIEMPRE es un problema de infraestructura (person-ms nunca lo usa para "sin datos").
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));

        // Act & Assert
        StepVerifier.create(personRestConsumer.findTopBootcampEnrollment())
                .expectError(ServiceUnavailableException.class)
                .verify(Duration.ofSeconds(2));
    }

    @Test
    void When_ServerRespondsWithTransientServerError_Expect_RetryUntilSuccess() {
        // Arrange: primer intento falla con 500, segundo intento (reintento) responde bien
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.OK.value())
                .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        { "bootcampId": %d, "enrolledPersonCount": %d, "persons": [] }
                        """.formatted(BOOTCAMP_ID, RETRY_RESPONSE_ENROLLED_PERSON_COUNT)));

        // Act & Assert
        StepVerifier.create(personRestConsumer.findTopBootcampEnrollment())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void When_ServerRepeatedlyRespondsWithServerError_Expect_ServiceUnavailableExceptionAfterRetries() {
        // Arrange: 1 intento inicial + 2 reintentos = 3 respuestas 500
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        // Act & Assert
        StepVerifier.create(personRestConsumer.findTopBootcampEnrollment())
                .expectError(ServiceUnavailableException.class)
                .verify(Duration.ofSeconds(2));
    }
}
