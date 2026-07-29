package co.com.pragma.usecase.findtopbootcamp;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.exceptions.BootcampNotFoundException;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.bootcamp.gateways.CapabilityGateway;
import co.com.pragma.model.bootcamp.gateways.PersonGateway;
import co.com.pragma.model.bootcamp.query.CapabilitySummary;
import co.com.pragma.model.bootcamp.query.EnrolledPersonSummary;
import co.com.pragma.model.bootcamp.query.TechnologySummary;
import co.com.pragma.model.bootcamp.query.TopBootcampEnrollmentSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindTopBootcampUseCaseTest {

    private static final Long BOOTCAMP_ID = 10L;
    private static final String VALID_NAME = "Java Backend Bootcamp";
    private static final String VALID_DESCRIPTION = "Bootcamp de backend con Java";
    private static final LocalDate VALID_LAUNCH_DATE = LocalDate.of(2026, 8, 1);
    private static final Integer VALID_DURATION_IN_WEEKS = 12;
    private static final Long CAPABILITY_ID = 1L;
    private static final String CAPABILITY_NAME = "Backend";
    private static final Long TECHNOLOGY_ID = 100L;
    private static final String TECHNOLOGY_NAME = "Java";
    private static final Long ENROLLED_PERSON_COUNT = 1L;
    private static final String PERSON_NAME = "Ada Lovelace";
    private static final String PERSON_EMAIL = "ada@mail.com";

    @Mock
    private PersonGateway personGateway;

    @Mock
    private BootcampRepository bootcampRepository;

    @Mock
    private CapabilityGateway capabilityGateway;

    private FindTopBootcampUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindTopBootcampUseCase(personGateway, bootcampRepository, capabilityGateway);
    }

    @Test
    void When_TopBootcampExists_Expect_ComposedResultWithCapabilitiesTechnologiesAndPersons() {
        // Arrange
        TechnologySummary technology = new TechnologySummary(TECHNOLOGY_ID, TECHNOLOGY_NAME);
        CapabilitySummary capability = new CapabilitySummary(CAPABILITY_ID, CAPABILITY_NAME, List.of(technology));
        EnrolledPersonSummary person = new EnrolledPersonSummary(PERSON_NAME, PERSON_EMAIL);
        TopBootcampEnrollmentSummary topBootcampEnrollment = new TopBootcampEnrollmentSummary(BOOTCAMP_ID,
                ENROLLED_PERSON_COUNT, List.of(person));
        Bootcamp bootcamp = validBootcamp();

        when(personGateway.findTopBootcampEnrollment()).thenReturn(Mono.just(topBootcampEnrollment));
        when(bootcampRepository.findById(BOOTCAMP_ID)).thenReturn(Mono.just(bootcamp));
        when(capabilityGateway.findCapabilitiesByBootcampIds(List.of(BOOTCAMP_ID)))
                .thenReturn(Mono.just(Map.of(BOOTCAMP_ID, List.of(capability))));

        // Act & Assert
        StepVerifier.create(useCase.execute())
                .expectNextMatches(result -> result.bootcamp().equals(bootcamp)
                        && result.capabilities().equals(List.of(capability))
                        && result.technologies().equals(List.of(technology))
                        && result.enrolledPersonCount().equals(ENROLLED_PERSON_COUNT)
                        && result.persons().equals(List.of(person)))
                .verifyComplete();
    }

    @Test
    void Expect_EmptyMono_When_PersonGatewayReturnsEmpty() {
        // Arrange
        when(personGateway.findTopBootcampEnrollment()).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.execute())
                .verifyComplete();
    }

    @Test
    void Expect_BootcampNotFoundException_When_WinningBootcampNoLongerExists() {
        // Arrange
        TopBootcampEnrollmentSummary topBootcampEnrollment = new TopBootcampEnrollmentSummary(BOOTCAMP_ID,
                ENROLLED_PERSON_COUNT, List.of());

        when(personGateway.findTopBootcampEnrollment()).thenReturn(Mono.just(topBootcampEnrollment));
        when(bootcampRepository.findById(BOOTCAMP_ID)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.execute())
                .expectError(BootcampNotFoundException.class)
                .verify();
    }

    private Bootcamp validBootcamp() {
        return Bootcamp.builder()
                .id(BOOTCAMP_ID)
                .name(VALID_NAME)
                .description(VALID_DESCRIPTION)
                .launchDate(VALID_LAUNCH_DATE)
                .durationInWeeks(VALID_DURATION_IN_WEEKS)
                .build();
    }
}
