package co.com.pragma.usecase.retrypendingbootcampdeletions;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.BootcampStatusEnum;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.bootcamp.gateways.CapabilityGateway;
import co.com.pragma.usecase.deletebootcamp.DeleteBootcampUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryPendingBootcampDeletionsUseCaseTest {

    private static final String VALID_NAME = "Java Backend Bootcamp";
    private static final String VALID_DESCRIPTION = "Bootcamp de backend con Java";
    private static final LocalDate VALID_LAUNCH_DATE = LocalDate.of(2026, 8, 1);
    private static final Integer VALID_DURATION_IN_WEEKS = 12;
    private static final Long BOOTCAMP_ID_1 = 10L;
    private static final Long BOOTCAMP_ID_2 = 11L;

    @Mock
    private BootcampRepository bootcampRepository;

    @Mock
    private CapabilityGateway capabilityGateway;

    private RetryPendingBootcampDeletionsUseCase useCase;

    @BeforeEach
    void setUp() {
        DeleteBootcampUseCase deleteBootcampUseCase = new DeleteBootcampUseCase(bootcampRepository, capabilityGateway);
        useCase = new RetryPendingBootcampDeletionsUseCase(bootcampRepository, deleteBootcampUseCase);
    }

    @Test
    void Expect_CascadeResumedForEveryPendingDeletion_When_PendingDeletionsExist() {
        // Arrange
        Bootcamp pendingDeletion1 = bootcampWithId(BOOTCAMP_ID_1);
        Bootcamp pendingDeletion2 = bootcampWithId(BOOTCAMP_ID_2);

        when(bootcampRepository.findAllPendingDeletion()).thenReturn(Mono.just(List.of(pendingDeletion1, pendingDeletion2)));
        when(capabilityGateway.deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID_1)).thenReturn(Mono.empty());
        when(capabilityGateway.deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID_2)).thenReturn(Mono.empty());
        when(bootcampRepository.deleteById(BOOTCAMP_ID_1)).thenReturn(Mono.empty());
        when(bootcampRepository.deleteById(BOOTCAMP_ID_2)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(useCase.execute())
                .verifyComplete();

        verify(capabilityGateway).deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID_1);
        verify(capabilityGateway).deleteOrphanedCapabilitiesForBootcamp(BOOTCAMP_ID_2);
        verify(bootcampRepository).deleteById(BOOTCAMP_ID_1);
        verify(bootcampRepository).deleteById(BOOTCAMP_ID_2);
    }

    @Test
    void Expect_NoCascadeCalls_When_NoPendingDeletionsExist() {
        // Arrange
        when(bootcampRepository.findAllPendingDeletion()).thenReturn(Mono.just(List.of()));

        // Act & Assert
        StepVerifier.create(useCase.execute())
                .verifyComplete();
    }

    private static Bootcamp bootcampWithId(Long id) {
        return Bootcamp.builder()
                .id(id)
                .version(0L)
                .name(VALID_NAME)
                .description(VALID_DESCRIPTION)
                .launchDate(VALID_LAUNCH_DATE)
                .durationInWeeks(VALID_DURATION_IN_WEEKS)
                .status(BootcampStatusEnum.DELETING)
                .build();
    }
}
