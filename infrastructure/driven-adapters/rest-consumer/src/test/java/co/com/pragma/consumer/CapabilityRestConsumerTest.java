package co.com.pragma.consumer;

import co.com.pragma.model.bootcamp.exceptions.CapabilityServiceUnavailableException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

class CapabilityRestConsumerTest {

    private static final Long BOOTCAMP_ID = 10L;
    private static final Long CAPABILITY_ID_1 = 1L;
    private static final Long CAPABILITY_ID_2 = 2L;
    private static final Long CAPABILITY_ID_3 = 3L;
    private static final List<Long> CAPABILITY_IDS = List.of(CAPABILITY_ID_1, CAPABILITY_ID_2, CAPABILITY_ID_3);

    private static CapabilityRestConsumer capabilityRestConsumer;

    private static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        capabilityRestConsumer = new CapabilityRestConsumer(webClient);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void When_AllCapabilitiesExist_Expect_EmptyMissingIdsList() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"missingIds\": []}"));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.checkCapabilitiesExistence(CAPABILITY_IDS))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void When_SomeCapabilitiesDoNotExist_Expect_MissingIdsListToBeReturned() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"missingIds\": [%d]}".formatted(CAPABILITY_ID_3)));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.checkCapabilitiesExistence(CAPABILITY_IDS))
                .expectNextMatches(missingIds -> missingIds.equals(List.of(CAPABILITY_ID_3)))
                .verifyComplete();
    }

    @Test
    void When_LinkingBootcampCapabilities_Expect_CompletionWithoutError() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.CREATED.value())
                .setBody("{\"bootcampId\": %d, \"capabilityIds\": [%d, %d, %d]}"
                        .formatted(BOOTCAMP_ID, CAPABILITY_ID_1, CAPABILITY_ID_2, CAPABILITY_ID_3)));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.linkBootcampCapabilities(BOOTCAMP_ID, CAPABILITY_IDS))
                .verifyComplete();
    }

    @Test
    void When_DeletingBootcampCapabilities_Expect_CompletionWithoutError() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value()));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.deleteBootcampCapabilities(BOOTCAMP_ID))
                .verifyComplete();
    }

    @Test
    void When_FindingCapabilitiesByBootcampIds_Expect_MapGroupedByBootcampWithNestedTechnologies() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("""
                        {
                          "bootcamps": [
                            {
                              "bootcampId": %d,
                              "capabilities": [
                                {
                                  "id": %d,
                                  "name": "Backend",
                                  "technologies": [
                                    { "id": 100, "name": "Java" }
                                  ]
                                }
                              ]
                            }
                          ]
                        }
                        """.formatted(BOOTCAMP_ID, CAPABILITY_ID_1)));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.findCapabilitiesByBootcampIds(List.of(BOOTCAMP_ID)))
                .expectNextMatches(result -> result.get(BOOTCAMP_ID).size() == 1
                        && result.get(BOOTCAMP_ID).get(0).technologies().size() == 1)
                .verifyComplete();
    }

    @Test
    void When_FindingCapabilitiesByBootcampIdsFails_Expect_CapabilityServiceUnavailableException() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody("{\"message\": \"Business validation failed\"}"));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.findCapabilitiesByBootcampIds(List.of(BOOTCAMP_ID)))
                .expectError(CapabilityServiceUnavailableException.class)
                .verify(Duration.ofSeconds(2));
    }

    @Test
    void When_ServerRespondsWithTransientServerError_Expect_RetryUntilSuccess() {
        // Arrange: primer intento falla con 500, segundo intento (reintento) responde bien
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"missingIds\": []}"));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.checkCapabilitiesExistence(CAPABILITY_IDS))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void When_ServerRespondsWithBusinessError_Expect_NoRetryAndCapabilityServiceUnavailableException() {
        // Arrange: solo se encola UNA respuesta 400; si el consumer reintentara,
        // la segunda llamada se quedaría esperando una respuesta que no existe y el test fallaría por timeout.
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody("{\"message\": \"Business validation failed\"}"));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.checkCapabilitiesExistence(CAPABILITY_IDS))
                .expectError(CapabilityServiceUnavailableException.class)
                .verify(Duration.ofSeconds(2));
    }

    @Test
    void When_LinkingBootcampCapabilitiesFails_Expect_CapabilityServiceUnavailableException() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody("{\"message\": \"Business validation failed\"}"));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.linkBootcampCapabilities(BOOTCAMP_ID, CAPABILITY_IDS))
                .expectError(CapabilityServiceUnavailableException.class)
                .verify(Duration.ofSeconds(2));
    }

    @Test
    void When_DeletingBootcampCapabilitiesFails_Expect_CapabilityServiceUnavailableException() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody("{\"message\": \"Business validation failed\"}"));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.deleteBootcampCapabilities(BOOTCAMP_ID))
                .expectError(CapabilityServiceUnavailableException.class)
                .verify(Duration.ofSeconds(2));
    }

    @Test
    void When_DeletingOrphanedCapabilitiesForBootcamp_Expect_CompletionWithoutError() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value()));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID))
                .verifyComplete();
    }

    @Test
    void When_DeletingOrphanedCapabilitiesForBootcampFails_Expect_CapabilityServiceUnavailableException() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody("{\"message\": \"Business validation failed\"}"));

        // Act & Assert
        StepVerifier.create(capabilityRestConsumer.deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID))
                .expectError(CapabilityServiceUnavailableException.class)
                .verify(Duration.ofSeconds(2));
    }
}
