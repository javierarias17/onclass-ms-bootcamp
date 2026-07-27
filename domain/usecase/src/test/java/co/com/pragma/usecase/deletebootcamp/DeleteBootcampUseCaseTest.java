package co.com.pragma.usecase.deletebootcamp;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.BootcampStatusEnum;
import co.com.pragma.model.bootcamp.exceptions.BootcampNotFoundException;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.bootcamp.gateways.CapabilityGateway;
import co.com.pragma.model.exceptions.FieldsValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteBootcampUseCaseTest {

    private static final String VALID_NAME = "Java Backend Bootcamp";
    private static final String VALID_DESCRIPTION = "Bootcamp de backend con Java";
    private static final LocalDate VALID_LAUNCH_DATE = LocalDate.of(2026, 8, 1);
    private static final Integer VALID_DURATION_IN_WEEKS = 12;
    private static final Long BOOTCAMP_ID = 10L;
    private static final String BOOTCAMP_ID_STRING = "10";
    private static final String NON_NUMERIC_ID = "abc";
    private static final String BLANK_VALUE = " ";

    @Mock
    private BootcampRepository bootcampRepository;

    @Mock
    private CapabilityGateway capabilityGateway;

    private DeleteBootcampUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteBootcampUseCase(bootcampRepository, capabilityGateway);
    }

    @Test
    void Expect_BootcampMarkedDeletingThenCascadedAndDeleted_When_BootcampIsCreated() {
        // Arrange
        Bootcamp createdBootcamp = bootcampWithStatus(BootcampStatusEnum.CREATED);
        Bootcamp deletingBootcamp = bootcampWithStatus(BootcampStatusEnum.DELETING);

        when(bootcampRepository.findById(BOOTCAMP_ID)).thenReturn(Mono.just(createdBootcamp));
        when(bootcampRepository.updateStatus(any(Bootcamp.class))).thenReturn(Mono.just(deletingBootcamp));
        when(capabilityGateway.deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID)).thenReturn(Mono.empty());
        when(bootcampRepository.deleteById(BOOTCAMP_ID)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.execute(BOOTCAMP_ID_STRING))
                .verifyComplete();

        verify(bootcampRepository).updateStatus(any(Bootcamp.class));
        verify(capabilityGateway).deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID);
        verify(bootcampRepository).deleteById(BOOTCAMP_ID);
    }

    @Test
    void Expect_CascadeResumedWithoutMarkingDeletingAgain_When_BootcampIsAlreadyDeleting() {
        // Arrange: un intento anterior murió a mitad de camino y dejó el bootcamp en DELETING
        Bootcamp deletingBootcamp = bootcampWithStatus(BootcampStatusEnum.DELETING);

        when(bootcampRepository.findById(BOOTCAMP_ID)).thenReturn(Mono.just(deletingBootcamp));
        when(capabilityGateway.deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID)).thenReturn(Mono.empty());
        when(bootcampRepository.deleteById(BOOTCAMP_ID)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.execute(BOOTCAMP_ID_STRING))
                .verifyComplete();

        verify(bootcampRepository, never()).updateStatus(any(Bootcamp.class));
        verify(capabilityGateway).deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID);
        verify(bootcampRepository).deleteById(BOOTCAMP_ID);
    }

    @Test
    void Expect_BootcampNotFoundException_When_BootcampDoesNotExist() {
        // Arrange
        when(bootcampRepository.findById(BOOTCAMP_ID)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.execute(BOOTCAMP_ID_STRING))
                .expectError(BootcampNotFoundException.class)
                .verify();

        verify(bootcampRepository, never()).updateStatus(any(Bootcamp.class));
        verify(bootcampRepository, never()).deleteById(any());
    }

    @Test
    void Expect_LocalDeleteToBeSkipped_When_CapabilityGatewayFails() {
        // Arrange: la cascada falla en capability-ms; el bootcamp debe permanecer en DELETING
        // para que el job de reintento lo repare, sin que se borre localmente todavía.
        Bootcamp createdBootcamp = bootcampWithStatus(BootcampStatusEnum.CREATED);
        Bootcamp deletingBootcamp = bootcampWithStatus(BootcampStatusEnum.DELETING);
        RuntimeException cascadeFailure = new RuntimeException("capability service unavailable");

        when(bootcampRepository.findById(BOOTCAMP_ID)).thenReturn(Mono.just(createdBootcamp));
        when(bootcampRepository.updateStatus(any(Bootcamp.class))).thenReturn(Mono.just(deletingBootcamp));
        when(capabilityGateway.deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID)).thenReturn(Mono.error(cascadeFailure));

        // Act & Assert
        StepVerifier.create(useCase.execute(BOOTCAMP_ID_STRING))
                .expectErrorMatches(error -> error == cascadeFailure)
                .verify();

        verify(bootcampRepository, never()).deleteById(any());
    }

    @Test
    void Expect_FieldsValidationException_When_IdIsBlank() {
        // Act & Assert
        StepVerifier.create(useCase.execute(BLANK_VALUE))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findById(any());
    }

    @Test
    void Expect_FieldsValidationException_When_IdIsNotNumeric() {
        // Act & Assert
        StepVerifier.create(useCase.execute(NON_NUMERIC_ID))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findById(any());
    }

    private static Bootcamp bootcampWithStatus(BootcampStatusEnum status) {
        return Bootcamp.builder()
                .id(BOOTCAMP_ID)
                .version(0L)
                .name(VALID_NAME)
                .description(VALID_DESCRIPTION)
                .launchDate(VALID_LAUNCH_DATE)
                .durationInWeeks(VALID_DURATION_IN_WEEKS)
                .status(status)
                .build();
    }
}
