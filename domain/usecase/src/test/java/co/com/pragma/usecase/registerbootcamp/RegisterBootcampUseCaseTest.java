package co.com.pragma.usecase.registerbootcamp;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.BootcampStatusEnum;
import co.com.pragma.model.bootcamp.command.BootcampCreateCommand;
import co.com.pragma.model.bootcamp.exceptions.BootcampAlreadyExistsException;
import co.com.pragma.model.bootcamp.exceptions.CapabilitiesNotFoundException;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.bootcamp.gateways.CapabilityGateway;
import co.com.pragma.model.bootcamp.valueobject.BootcampCapabilityIds;
import co.com.pragma.model.exceptions.FieldsValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterBootcampUseCaseTest {

    private static final String VALID_NAME = "Java Backend Bootcamp";
    private static final String VALID_DESCRIPTION = "Bootcamp de backend con Java";
    private static final LocalDate VALID_LAUNCH_DATE = LocalDate.of(2026, 8, 1);
    private static final Integer VALID_DURATION_IN_WEEKS = 12;
    private static final Long BOOTCAMP_ID = 10L;
    private static final Long CAPABILITY_ID_1 = 1L;
    private static final Long CAPABILITY_ID_2 = 2L;
    private static final List<Long> VALID_CAPABILITY_IDS = List.of(CAPABILITY_ID_1, CAPABILITY_ID_2);
    private static final List<Long> MISSING_CAPABILITY_IDS = List.of(CAPABILITY_ID_2);
    private static final String BLANK_VALUE = " ";

    @Mock
    private BootcampRepository bootcampRepository;

    @Mock
    private CapabilityGateway capabilityGateway;

    private RegisterBootcampUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterBootcampUseCase(bootcampRepository, capabilityGateway);
    }

    @Test
    void When_BootcampInformationIsValid_Expect_BootcampToBeSavedAndLinked() {
        // Arrange
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, VALID_CAPABILITY_IDS);
        Bootcamp creatingBootcamp = bootcampWithStatus(BOOTCAMP_ID, BootcampStatusEnum.CREATING);
        Bootcamp createdBootcamp = bootcampWithStatus(BOOTCAMP_ID, BootcampStatusEnum.CREATED);

        when(bootcampRepository.findByName(VALID_NAME)).thenReturn(Mono.empty());
        when(capabilityGateway.checkCapabilitiesExistence(VALID_CAPABILITY_IDS)).thenReturn(Mono.just(List.of()));
        when(bootcampRepository.save(any(Bootcamp.class)))
                .thenReturn(Mono.just(creatingBootcamp), Mono.just(createdBootcamp));
        when(capabilityGateway.linkBootcampCapabilities(BOOTCAMP_ID, VALID_CAPABILITY_IDS)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectNextMatches(result -> result.getId().equals(BOOTCAMP_ID)
                        && result.getName().value().equals(VALID_NAME)
                        && result.getStatus() == BootcampStatusEnum.CREATED)
                .verifyComplete();

        // registro nuevo: no hay vínculos previos que limpiar
        verify(capabilityGateway, never()).deleteBootcampCapabilities(anyLong());
        verify(bootcampRepository, times(2)).save(any(Bootcamp.class));
    }

    @Test
    void Expect_CapabilitiesNotFoundException_When_SomeCapabilitiesDoNotExist() {
        // Arrange
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, VALID_CAPABILITY_IDS);

        when(bootcampRepository.findByName(VALID_NAME)).thenReturn(Mono.empty());
        when(capabilityGateway.checkCapabilitiesExistence(VALID_CAPABILITY_IDS)).thenReturn(Mono.just(MISSING_CAPABILITY_IDS));

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(CapabilitiesNotFoundException.class)
                .verify();

        verify(bootcampRepository, never()).save(any());
    }

    @Test
    void Expect_BootcampAlreadyExistsException_When_ExistingBootcampIsCreated() {
        // Arrange
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, VALID_CAPABILITY_IDS);
        Bootcamp createdBootcamp = bootcampWithStatus(BOOTCAMP_ID, BootcampStatusEnum.CREATED);

        when(bootcampRepository.findByName(VALID_NAME)).thenReturn(Mono.just(createdBootcamp));

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(BootcampAlreadyExistsException.class)
                .verify();

        verify(capabilityGateway, never()).checkCapabilitiesExistence(anyList());
        verify(bootcampRepository, never()).save(any());
    }

    @Test
    void Expect_BootcampAlreadyExistsException_When_ExistingBootcampIsDeleting() {
        // Arrange: el bootcamp está siendo eliminado (DELETING); no debe "resucitarse" vía registro
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, VALID_CAPABILITY_IDS);
        Bootcamp deletingBootcamp = bootcampWithStatus(BOOTCAMP_ID, BootcampStatusEnum.DELETING);

        when(bootcampRepository.findByName(VALID_NAME)).thenReturn(Mono.just(deletingBootcamp));

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(BootcampAlreadyExistsException.class)
                .verify();

        verify(capabilityGateway, never()).checkCapabilitiesExistence(anyList());
        verify(bootcampRepository, never()).save(any());
    }

    @Test
    void Expect_BootcampToBeResumedAndCreated_When_ExistingBootcampIsCreating() {
        // Arrange: un intento anterior murió a mitad de camino y dejó el bootcamp en CREATING
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, VALID_CAPABILITY_IDS);
        Bootcamp creatingBootcamp = bootcampWithStatus(BOOTCAMP_ID, BootcampStatusEnum.CREATING);
        Bootcamp createdBootcamp = bootcampWithStatus(BOOTCAMP_ID, BootcampStatusEnum.CREATED);

        when(bootcampRepository.findByName(VALID_NAME)).thenReturn(Mono.just(creatingBootcamp));
        when(capabilityGateway.checkCapabilitiesExistence(VALID_CAPABILITY_IDS)).thenReturn(Mono.just(List.of()));
        when(bootcampRepository.save(argThat(b -> b != null && BOOTCAMP_ID.equals(b.getId()))))
                .thenReturn(Mono.just(creatingBootcamp), Mono.just(createdBootcamp));
        when(capabilityGateway.deleteBootcampCapabilities(BOOTCAMP_ID)).thenReturn(Mono.empty());
        when(capabilityGateway.linkBootcampCapabilities(BOOTCAMP_ID, VALID_CAPABILITY_IDS)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectNextMatches(result -> result.getId().equals(BOOTCAMP_ID)
                        && result.getStatus() == BootcampStatusEnum.CREATED)
                .verifyComplete();

        // el mismo id se reutiliza y se limpian los vínculos parciales antes de volver a enlazar
        verify(capabilityGateway).deleteBootcampCapabilities(BOOTCAMP_ID);
        verify(capabilityGateway).linkBootcampCapabilities(BOOTCAMP_ID, VALID_CAPABILITY_IDS);
    }

    @Test
    void Expect_BootcampToRemainCreating_When_LinkingCapabilitiesFails() {
        // Arrange
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, VALID_CAPABILITY_IDS);
        Bootcamp creatingBootcamp = bootcampWithStatus(BOOTCAMP_ID, BootcampStatusEnum.CREATING);
        RuntimeException linkFailure = new RuntimeException("capability service unavailable");

        when(bootcampRepository.findByName(VALID_NAME)).thenReturn(Mono.empty());
        when(capabilityGateway.checkCapabilitiesExistence(VALID_CAPABILITY_IDS)).thenReturn(Mono.just(List.of()));
        when(bootcampRepository.save(any(Bootcamp.class))).thenReturn(Mono.just(creatingBootcamp));
        when(capabilityGateway.linkBootcampCapabilities(BOOTCAMP_ID, VALID_CAPABILITY_IDS)).thenReturn(Mono.error(linkFailure));

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectErrorMatches(error -> error == linkFailure)
                .verify();

        // no se marca CREATED: el bootcamp queda en CREATING para que un reintento lo repare
        verify(bootcampRepository, times(1)).save(any(Bootcamp.class));
    }

    @Test
    void Expect_FieldsValidationException_When_NameIsBlank() {
        // Arrange
        BootcampCreateCommand command = new BootcampCreateCommand(BLANK_VALUE, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, VALID_CAPABILITY_IDS);

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(capabilityGateway, never()).checkCapabilitiesExistence(anyList());
    }

    @Test
    void Expect_FieldsValidationException_When_DescriptionIsBlank() {
        // Arrange
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, BLANK_VALUE,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, VALID_CAPABILITY_IDS);

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(capabilityGateway, never()).checkCapabilitiesExistence(anyList());
    }

    @Test
    void Expect_FieldsValidationException_When_LaunchDateIsNull() {
        // Arrange
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                null, VALID_DURATION_IN_WEEKS, VALID_CAPABILITY_IDS);

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(capabilityGateway, never()).checkCapabilitiesExistence(anyList());
    }

    @Test
    void Expect_FieldsValidationException_When_DurationInWeeksIsNull() {
        // Arrange
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, null, VALID_CAPABILITY_IDS);

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(capabilityGateway, never()).checkCapabilitiesExistence(anyList());
    }

    @Test
    void Expect_FieldsValidationException_When_DurationInWeeksIsZeroOrNegative() {
        // Arrange
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, 0, VALID_CAPABILITY_IDS);

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(capabilityGateway, never()).checkCapabilitiesExistence(anyList());
    }

    @Test
    void Expect_FieldsValidationException_When_CapabilityIdsIsEmpty() {
        // Arrange: un bootcamp debe tener como mínimo 1 capacidad asociada
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, List.of());

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(capabilityGateway, never()).checkCapabilitiesExistence(anyList());
    }

    @Test
    void Expect_FieldsValidationException_When_CapabilityIdsExceedsMaxSize() {
        // Arrange: un bootcamp debe tener como máximo 4 capacidades asociadas
        List<Long> tooManyIds = LongStream.rangeClosed(1, BootcampCapabilityIds.MAX_SIZE + 1).boxed().toList();
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, tooManyIds);

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(capabilityGateway, never()).checkCapabilitiesExistence(anyList());
    }

    @Test
    void Expect_FieldsValidationException_When_CapabilityIdsHasDuplicates() {
        // Arrange
        List<Long> duplicatedIds = List.of(CAPABILITY_ID_1, CAPABILITY_ID_1);
        BootcampCreateCommand command = new BootcampCreateCommand(VALID_NAME, VALID_DESCRIPTION,
                VALID_LAUNCH_DATE, VALID_DURATION_IN_WEEKS, duplicatedIds);

        // Act & Assert
        StepVerifier.create(useCase.execute(command))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(capabilityGateway, never()).checkCapabilitiesExistence(anyList());
    }

    private static Bootcamp bootcampWithStatus(Long id, BootcampStatusEnum status) {
        return Bootcamp.builder()
                .id(id)
                .name(VALID_NAME)
                .description(VALID_DESCRIPTION)
                .launchDate(VALID_LAUNCH_DATE)
                .durationInWeeks(VALID_DURATION_IN_WEEKS)
                .status(status)
                .build();
    }
}
