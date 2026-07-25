package co.com.pragma.usecase.listbootcamps;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.bootcamp.gateways.CapabilityGateway;
import co.com.pragma.model.bootcamp.query.BootcampListQuery;
import co.com.pragma.model.bootcamp.query.BootcampSortFieldEnum;
import co.com.pragma.model.bootcamp.query.CapabilitySummary;
import co.com.pragma.model.bootcamp.query.SortDirectionEnum;
import co.com.pragma.model.bootcamp.query.TechnologySummary;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListBootcampsUseCaseTest {

    private static final String VALID_NAME = "Java Backend Bootcamp";
    private static final String VALID_DESCRIPTION = "Bootcamp de backend con Java";
    private static final LocalDate VALID_LAUNCH_DATE = LocalDate.of(2026, 8, 1);
    private static final Integer VALID_DURATION_IN_WEEKS = 12;
    private static final Long BOOTCAMP_ID = 1L;
    private static final Long CAPABILITY_ID = 10L;
    private static final String CAPABILITY_NAME = "Backend";
    private static final Long OTHER_CAPABILITY_ID = 11L;
    private static final String OTHER_CAPABILITY_NAME = "DevOps";
    private static final Long TECHNOLOGY_ID = 100L;
    private static final String TECHNOLOGY_NAME = "Java";
    private static final Long OTHER_TECHNOLOGY_ID = 101L;
    private static final String OTHER_TECHNOLOGY_NAME = "Docker";
    private static final long ONE_ELEMENT = 1L;
    private static final long ZERO_ELEMENTS = 0L;

    private static final String PAGE = "0";
    private static final String SIZE = "10";
    private static final int PAGE_VALUE = 0;
    private static final int SIZE_VALUE = 10;
    private static final String SORT_BY_NAME = "NAME";
    private static final String SORT_BY_CAPABILITY_COUNT = "CAPABILITY_COUNT";
    private static final String SORT_DIRECTION_ASC = "ASC";
    private static final String SORT_DIRECTION_DESC = "DESC";

    private static final String BLANK_VALUE = " ";
    private static final String NOT_NUMERIC_VALUE = "abc";
    private static final String NEGATIVE_PAGE = "-1";
    private static final String OVERFLOWING_PAGE = "99999999999";
    private static final String ZERO_SIZE = "0";
    private static final String SIZE_EXCEEDING_MAX = "101";
    private static final String INVALID_SORT_FIELD = "invalidField";
    private static final String INVALID_SORT_DIRECTION = "invalidDirection";

    @Mock
    private BootcampRepository bootcampRepository;

    @Mock
    private CapabilityGateway capabilityGateway;

    private ListBootcampsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListBootcampsUseCase(bootcampRepository, capabilityGateway);
    }

    @Test
    void When_BootcampsExist_Expect_PageEnrichedWithCapabilitiesAndTechnologies() {
        // Arrange: capacidades y tecnologías se exponen como dos listas planas independientes
        // en el BootcampListItem, aunque el gateway las entregue anidadas (capacidad -> sus tecnologías).
        Bootcamp bootcamp = bootcampWithId(BOOTCAMP_ID);
        BootcampListQuery query = new BootcampListQuery(PAGE, SIZE, SORT_BY_NAME, SORT_DIRECTION_ASC);
        List<CapabilitySummary> capabilities = List.of(
                new CapabilitySummary(CAPABILITY_ID, CAPABILITY_NAME, List.of(new TechnologySummary(TECHNOLOGY_ID, TECHNOLOGY_NAME))));
        Map<Long, List<CapabilitySummary>> capabilitiesByBootcamp = Map.of(BOOTCAMP_ID, capabilities);

        when(bootcampRepository.findPage(PAGE_VALUE, SIZE_VALUE, BootcampSortFieldEnum.NAME, SortDirectionEnum.ASC))
                .thenReturn(Mono.just(List.of(bootcamp)));
        when(bootcampRepository.count()).thenReturn(Mono.just(ONE_ELEMENT));
        when(capabilityGateway.findCapabilitiesByBootcampIds(List.of(BOOTCAMP_ID)))
                .thenReturn(Mono.just(capabilitiesByBootcamp));

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectNextMatches(page -> page.content().size() == 1
                        && page.content().get(0).capabilities().size() == 1
                        && page.content().get(0).technologies().size() == 1
                        && page.totalElements() == ONE_ELEMENT
                        && page.totalPages() == 1)
                .verifyComplete();
    }

    @Test
    void When_CapabilitiesShareTechnologies_Expect_TechnologiesDeduplicatedByIdAcrossCapabilities() {
        // Arrange: dos capacidades del mismo bootcamp comparten la tecnología "Java" (id 100);
        // el listado de tecnologías del bootcamp debe traerla una sola vez.
        Bootcamp bootcamp = bootcampWithId(BOOTCAMP_ID);
        BootcampListQuery query = new BootcampListQuery(PAGE, SIZE, SORT_BY_NAME, SORT_DIRECTION_ASC);
        List<CapabilitySummary> capabilities = List.of(
                new CapabilitySummary(CAPABILITY_ID, CAPABILITY_NAME,
                        List.of(new TechnologySummary(TECHNOLOGY_ID, TECHNOLOGY_NAME))),
                new CapabilitySummary(OTHER_CAPABILITY_ID, OTHER_CAPABILITY_NAME,
                        List.of(new TechnologySummary(TECHNOLOGY_ID, TECHNOLOGY_NAME),
                                new TechnologySummary(OTHER_TECHNOLOGY_ID, OTHER_TECHNOLOGY_NAME))));
        Map<Long, List<CapabilitySummary>> capabilitiesByBootcamp = Map.of(BOOTCAMP_ID, capabilities);

        when(bootcampRepository.findPage(PAGE_VALUE, SIZE_VALUE, BootcampSortFieldEnum.NAME, SortDirectionEnum.ASC))
                .thenReturn(Mono.just(List.of(bootcamp)));
        when(bootcampRepository.count()).thenReturn(Mono.just(ONE_ELEMENT));
        when(capabilityGateway.findCapabilitiesByBootcampIds(List.of(BOOTCAMP_ID)))
                .thenReturn(Mono.just(capabilitiesByBootcamp));

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectNextMatches(page -> page.content().get(0).capabilities().size() == 2
                        && page.content().get(0).technologies().size() == 2)
                .verifyComplete();
    }

    @Test
    void When_NoBootcampsExist_Expect_EmptyPageWithoutCallingCapabilityGateway() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(PAGE, SIZE, SORT_BY_NAME, SORT_DIRECTION_ASC);

        when(bootcampRepository.findPage(PAGE_VALUE, SIZE_VALUE, BootcampSortFieldEnum.NAME, SortDirectionEnum.ASC))
                .thenReturn(Mono.just(List.of()));
        when(bootcampRepository.count()).thenReturn(Mono.just(ZERO_ELEMENTS));

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectNextMatches(page -> page.content().isEmpty() && page.totalElements() == ZERO_ELEMENTS)
                .verifyComplete();

        verify(capabilityGateway, never()).findCapabilitiesByBootcampIds(any());
    }

    @Test
    void When_SortByCapabilityCountDesc_Expect_RepositoryCalledWithMappedSort() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(PAGE, SIZE, SORT_BY_CAPABILITY_COUNT, SORT_DIRECTION_DESC);

        when(bootcampRepository.findPage(PAGE_VALUE, SIZE_VALUE, BootcampSortFieldEnum.CAPABILITY_COUNT, SortDirectionEnum.DESC))
                .thenReturn(Mono.just(List.of()));
        when(bootcampRepository.count()).thenReturn(Mono.just(ZERO_ELEMENTS));

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void Expect_ErrorToPropagate_When_CapabilityGatewayFails() {
        // Arrange
        Bootcamp bootcamp = bootcampWithId(BOOTCAMP_ID);
        BootcampListQuery query = new BootcampListQuery(PAGE, SIZE, SORT_BY_NAME, SORT_DIRECTION_ASC);
        RuntimeException failure = new RuntimeException("capability service unavailable");

        when(bootcampRepository.findPage(PAGE_VALUE, SIZE_VALUE, BootcampSortFieldEnum.NAME, SortDirectionEnum.ASC))
                .thenReturn(Mono.just(List.of(bootcamp)));
        when(bootcampRepository.count()).thenReturn(Mono.just(ONE_ELEMENT));
        when(capabilityGateway.findCapabilitiesByBootcampIds(List.of(BOOTCAMP_ID)))
                .thenReturn(Mono.error(failure));

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectErrorMatches(error -> error == failure)
                .verify();
    }

    @Test
    void Expect_FieldsValidationException_When_PageIsNotNumeric() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(NOT_NUMERIC_VALUE, SIZE, SORT_BY_NAME, SORT_DIRECTION_ASC);

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findPage(anyInt(), anyInt(), any(), any());
    }

    @Test
    void Expect_FieldsValidationException_When_PageIsNegative() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(NEGATIVE_PAGE, SIZE, SORT_BY_NAME, SORT_DIRECTION_ASC);

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findPage(anyInt(), anyInt(), any(), any());
    }

    @Test
    void Expect_FieldsValidationException_When_PageOverflowsIntegerRange() {
        // Arrange: numérico según el regex, pero no cabe en un int
        BootcampListQuery query = new BootcampListQuery(OVERFLOWING_PAGE, SIZE, SORT_BY_NAME, SORT_DIRECTION_ASC);

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findPage(anyInt(), anyInt(), any(), any());
    }

    @Test
    void Expect_FieldsValidationException_When_PageIsBlank() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(BLANK_VALUE, SIZE, SORT_BY_NAME, SORT_DIRECTION_ASC);

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findPage(anyInt(), anyInt(), any(), any());
    }

    @Test
    void Expect_FieldsValidationException_When_SizeIsBlank() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(PAGE, BLANK_VALUE, SORT_BY_NAME, SORT_DIRECTION_ASC);

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findPage(anyInt(), anyInt(), any(), any());
    }

    @Test
    void Expect_FieldsValidationException_When_SizeIsNotNumeric() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(PAGE, NOT_NUMERIC_VALUE, SORT_BY_NAME, SORT_DIRECTION_ASC);

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findPage(anyInt(), anyInt(), any(), any());
    }

    @Test
    void Expect_FieldsValidationException_When_SizeIsOutOfRange() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(PAGE, ZERO_SIZE, SORT_BY_NAME, SORT_DIRECTION_ASC);

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findPage(anyInt(), anyInt(), any(), any());
    }

    @Test
    void Expect_FieldsValidationException_When_SizeExceedsMax() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(PAGE, SIZE_EXCEEDING_MAX, SORT_BY_NAME, SORT_DIRECTION_ASC);

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findPage(anyInt(), anyInt(), any(), any());
    }

    @Test
    void Expect_FieldsValidationException_When_SortByIsInvalid() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(PAGE, SIZE, INVALID_SORT_FIELD, SORT_DIRECTION_ASC);

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findPage(anyInt(), anyInt(), any(), any());
    }

    @Test
    void Expect_FieldsValidationException_When_SortDirectionIsInvalid() {
        // Arrange
        BootcampListQuery query = new BootcampListQuery(PAGE, SIZE, SORT_BY_NAME, INVALID_SORT_DIRECTION);

        // Act & Assert
        StepVerifier.create(useCase.execute(query))
                .expectError(FieldsValidationException.class)
                .verify();

        verify(bootcampRepository, never()).findPage(anyInt(), anyInt(), any(), any());
    }

    private static Bootcamp bootcampWithId(Long id) {
        return Bootcamp.builder()
                .id(id)
                .name(VALID_NAME)
                .description(VALID_DESCRIPTION)
                .launchDate(VALID_LAUNCH_DATE)
                .durationInWeeks(VALID_DURATION_IN_WEEKS)
                .build();
    }
}
