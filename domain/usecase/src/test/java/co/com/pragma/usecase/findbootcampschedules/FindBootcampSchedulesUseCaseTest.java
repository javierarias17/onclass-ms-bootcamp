package co.com.pragma.usecase.findbootcampschedules;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.BootcampStatusEnum;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.exceptions.FieldsValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindBootcampSchedulesUseCaseTest {

    private static final Long BOOTCAMP_ID_1 = 1L;
    private static final Long BOOTCAMP_ID_2 = 2L;

    @Mock
    private BootcampRepository bootcampRepository;

    private FindBootcampSchedulesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindBootcampSchedulesUseCase(bootcampRepository);
    }

    @Test
    void When_BootcampIdsExist_Expect_MatchingBootcampsReturned() {
        // Arrange
        List<Long> bootcampIds = List.of(BOOTCAMP_ID_1, BOOTCAMP_ID_2);
        Bootcamp bootcamp = Bootcamp.builder()
                .id(BOOTCAMP_ID_1).name("Java Backend Bootcamp").description("Bootcamp de backend con Java")
                .launchDate(LocalDate.of(2026, 8, 1)).durationInWeeks(12).status(BootcampStatusEnum.CREATED)
                .build();

        when(bootcampRepository.findByIds(bootcampIds)).thenReturn(Flux.just(bootcamp));

        // Act & Assert
        StepVerifier.create(useCase.execute(bootcampIds))
                .expectNextMatches(result -> result.equals(List.of(bootcamp)))
                .verifyComplete();
    }

    @Test
    void Expect_FieldsValidationException_When_BootcampIdsListIsEmpty() {
        // Act & Assert
        StepVerifier.create(useCase.execute(List.of()))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findByIds(anyList());
    }

    @Test
    void Expect_FieldsValidationException_When_BootcampIdsListIsNull() {
        // Act & Assert
        StepVerifier.create(useCase.execute(null))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findByIds(anyList());
    }
}
