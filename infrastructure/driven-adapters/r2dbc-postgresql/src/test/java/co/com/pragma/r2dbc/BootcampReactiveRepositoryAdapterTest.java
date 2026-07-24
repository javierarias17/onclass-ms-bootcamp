package co.com.pragma.r2dbc;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.BootcampStatusEnum;
import co.com.pragma.model.bootcamp.exceptions.BootcampAlreadyExistsException;
import co.com.pragma.r2dbc.entity.BootcampEntity;
import co.com.pragma.r2dbc.mapper.BootcampEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampReactiveRepositoryAdapterTest {

    private static final String VALID_NAME = "Java Backend Bootcamp";
    private static final String VALID_DESCRIPTION = "Bootcamp de backend con Java";
    private static final LocalDate VALID_LAUNCH_DATE = LocalDate.of(2026, 8, 1);
    private static final Integer VALID_DURATION_IN_WEEKS = 12;
    private static final Long BOOTCAMP_ID = 1L;

    @Mock
    private BootcampReactiveRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private BootcampEntityMapper bootcampEntityMapper;

    private BootcampReactiveRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BootcampReactiveRepositoryAdapter(repository, mapper, bootcampEntityMapper);
    }

    @Test
    void Expect_BootcampAlreadyExistsException_When_ConcurrentInsertViolatesUniqueIndex() {
        // Arrange: dos requests concurrentes con el mismo nombre pasan el chequeo previo
        // y ambas intentan el INSERT; la segunda choca contra el índice único en Postgres.
        Bootcamp bootcamp = Bootcamp.builder()
                .name(VALID_NAME)
                .description(VALID_DESCRIPTION)
                .launchDate(VALID_LAUNCH_DATE)
                .durationInWeeks(VALID_DURATION_IN_WEEKS)
                .status(BootcampStatusEnum.PENDING)
                .build();
        BootcampEntity entity = BootcampEntity.builder()
                .name(VALID_NAME)
                .description(VALID_DESCRIPTION)
                .launchDate(VALID_LAUNCH_DATE)
                .durationInWeeks(VALID_DURATION_IN_WEEKS)
                .status(BootcampStatusEnum.PENDING.name())
                .build();

        when(bootcampEntityMapper.toEntity(bootcamp)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(Mono.error(
                new DuplicateKeyException("duplicate key value violates unique constraint")));

        // Act & Assert
        StepVerifier.create(adapter.save(bootcamp))
                .expectError(BootcampAlreadyExistsException.class)
                .verify();
    }

    @Test
    void When_FindByName_Expect_RepositoryToBeCalledAndMappedToDomain() {
        // Arrange
        BootcampEntity entity = BootcampEntity.builder()
                .id(BOOTCAMP_ID)
                .name(VALID_NAME)
                .description(VALID_DESCRIPTION)
                .launchDate(VALID_LAUNCH_DATE)
                .durationInWeeks(VALID_DURATION_IN_WEEKS)
                .status(BootcampStatusEnum.COMPLETE.name())
                .build();
        Bootcamp domain = Bootcamp.builder()
                .id(BOOTCAMP_ID)
                .name(VALID_NAME)
                .description(VALID_DESCRIPTION)
                .launchDate(VALID_LAUNCH_DATE)
                .durationInWeeks(VALID_DURATION_IN_WEEKS)
                .status(BootcampStatusEnum.COMPLETE)
                .build();

        when(repository.findByNameIgnoreCase(VALID_NAME)).thenReturn(Mono.just(entity));
        when(bootcampEntityMapper.toDomain(entity)).thenReturn(domain);

        // Act & Assert
        StepVerifier.create(adapter.findByName(VALID_NAME))
                .expectNextMatches(result -> result.getId().equals(BOOTCAMP_ID))
                .verifyComplete();
    }
}
