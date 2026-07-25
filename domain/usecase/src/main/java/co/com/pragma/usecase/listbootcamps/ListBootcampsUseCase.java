package co.com.pragma.usecase.listbootcamps;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.bootcamp.gateways.CapabilityGateway;
import co.com.pragma.model.bootcamp.query.BootcampListItem;
import co.com.pragma.model.bootcamp.query.BootcampListQuery;
import co.com.pragma.model.bootcamp.query.BootcampPage;
import co.com.pragma.model.bootcamp.query.BootcampSortFieldEnum;
import co.com.pragma.model.bootcamp.query.CapabilitySummary;
import co.com.pragma.model.bootcamp.query.SortDirectionEnum;
import co.com.pragma.model.bootcamp.query.TechnologySummary;
import co.com.pragma.model.common.FieldConstants;
import co.com.pragma.model.common.ValidationMessageConstants;
import co.com.pragma.model.common.validator.FieldValidator;
import co.com.pragma.model.exceptions.FieldsValidationException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListBootcampsUseCase {

        private static final int MIN_PAGE = 0;
        private static final int MIN_SIZE = 1;
        private static final int MAX_SIZE = 100;

        private final BootcampRepository bootcampRepository;
        private final CapabilityGateway capabilityGateway;

        public Mono<BootcampPage> execute(BootcampListQuery query) {
                Map<String, String> errors = collectFieldFormatErrors(query);

                if (!errors.isEmpty())
                        return Mono.error(new FieldsValidationException(errors));

                return Mono.fromCallable(() -> buildValidatedParams(query))
                                .flatMap(params -> Mono.zip(
                                                bootcampRepository.findPage(params.page(), params.size(),
                                                                params.sortField(), params.direction()),
                                                bootcampRepository.count())
                                                .flatMap(tuple -> buildBootcampPage(tuple.getT1(), tuple.getT2(),
                                                                params.page(), params.size())));
        }

        private ValidatedParams buildValidatedParams(BootcampListQuery query) {
                return ValidatedParams.builder()
                                .page(Integer.parseInt(query.page()))
                                .size(Integer.parseInt(query.size()))
                                .sortField(BootcampSortFieldEnum.valueOf(query.sortBy()))
                                .direction(SortDirectionEnum.valueOf(query.sortDirection()))
                                .build();
        }

        private Mono<BootcampPage> buildBootcampPage(List<Bootcamp> bootcamps, long totalElements, int page, int size) {
                int totalPages = (int) Math.ceil((double) totalElements / size);

                if (bootcamps.isEmpty())
                        return Mono.just(new BootcampPage(List.of(), page, size, totalElements, totalPages));

                List<Long> bootcampIds = bootcamps.stream().map(Bootcamp::getId).toList();

                return capabilityGateway.findCapabilitiesByBootcampIds(bootcampIds)
                                .map(capabilitiesByBootcamp -> bootcamps.stream()
                                                .map(bootcamp -> toBootcampListItem(bootcamp,
                                                                capabilitiesByBootcamp.getOrDefault(bootcamp.getId(),
                                                                                List.of())))
                                                .toList())
                                .map(content -> new BootcampPage(content, page, size, totalElements, totalPages));
        }

        private BootcampListItem toBootcampListItem(Bootcamp bootcamp, List<CapabilitySummary> capabilities) {
                List<TechnologySummary> technologies = capabilities.stream()
                                .flatMap(capability -> capability.technologies().stream())
                                .distinct()
                                .toList();

                return new BootcampListItem(bootcamp, capabilities, technologies);
        }

        private Map<String, String> collectFieldFormatErrors(BootcampListQuery query) {
                Map<String, String> errors = new LinkedHashMap<>();

                FieldValidator.validateNotBlank(query.page(), FieldConstants.PAGE,
                                ValidationMessageConstants.MSG_PAGE_MUST_BE_NUMERIC, errors);
                FieldValidator.validateNumericFormat(query.page(), FieldConstants.PAGE,
                                ValidationMessageConstants.MSG_PAGE_MUST_BE_NUMERIC, errors);
                if (!errors.containsKey(FieldConstants.PAGE))
                        FieldValidator.validateIntegerRange(query.page(), MIN_PAGE, Integer.MAX_VALUE,
                                        FieldConstants.PAGE,
                                        ValidationMessageConstants.MSG_PAGE_OUT_OF_RANGE, errors);

                FieldValidator.validateNotBlank(query.size(), FieldConstants.SIZE,
                                ValidationMessageConstants.MSG_SIZE_MUST_BE_NUMERIC, errors);
                FieldValidator.validateNumericFormat(query.size(), FieldConstants.SIZE,
                                ValidationMessageConstants.MSG_SIZE_MUST_BE_NUMERIC, errors);
                if (!errors.containsKey(FieldConstants.SIZE))
                        FieldValidator.validateIntegerRange(query.size(), MIN_SIZE, MAX_SIZE, FieldConstants.SIZE,
                                        String.format(ValidationMessageConstants.MSG_SIZE_OUT_OF_RANGE, MIN_SIZE,
                                                        MAX_SIZE),
                                        errors);

                FieldValidator.validateAllowedValue(query.sortBy(),
                                Arrays.stream(BootcampSortFieldEnum.values()).map(Enum::name)
                                                .collect(Collectors.toSet()),
                                FieldConstants.SORT_BY, ValidationMessageConstants.MSG_SORT_BY_INVALID, errors);

                FieldValidator.validateAllowedValue(query.sortDirection(),
                                Arrays.stream(SortDirectionEnum.values()).map(Enum::name).collect(Collectors.toSet()),
                                FieldConstants.SORT_DIRECTION, ValidationMessageConstants.MSG_SORT_DIRECTION_INVALID,
                                errors);

                return errors;
        }

        @Builder
        private record ValidatedParams(int page, int size, BootcampSortFieldEnum sortField,
                        SortDirectionEnum direction) {
        }
}
