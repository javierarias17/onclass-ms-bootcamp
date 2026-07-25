package co.com.pragma.r2dbc;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.exceptions.BootcampAlreadyExistsException;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.bootcamp.query.BootcampSortFieldEnum;
import co.com.pragma.model.bootcamp.query.SortDirectionEnum;
import co.com.pragma.model.common.FieldConstants;
import co.com.pragma.model.exceptions.constant.FunctionalMessageConstants;
import co.com.pragma.r2dbc.entity.BootcampEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.r2dbc.mapper.BootcampEntityMapper;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Repository
public class BootcampReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Bootcamp,
        BootcampEntity,
        Long,
        BootcampReactiveRepository
        > implements BootcampRepository {

    private static final String STATUS_COLUMN = "status";

    private final BootcampEntityMapper bootcampEntityMapper;
    private final R2dbcEntityTemplate template;

    public BootcampReactiveRepositoryAdapter(BootcampReactiveRepository repository, ObjectMapper mapper,
                                              BootcampEntityMapper bootcampEntityMapper,
                                              R2dbcEntityTemplate template) {
        super(repository, mapper, bootcampEntityMapper::toDomain);
        this.bootcampEntityMapper = bootcampEntityMapper;
        this.template = template;
    }

    @Override
    protected BootcampEntity toData(Bootcamp bootcamp) {
        return bootcampEntityMapper.toEntity(bootcamp);
    }

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        return super.save(bootcamp)
                .onErrorMap(DuplicateKeyException.class, ex -> new BootcampAlreadyExistsException(
                        FunctionalMessageConstants.BUSINESS_VALIDATION_FAILED,
                        Map.of(FieldConstants.NAME, FunctionalMessageConstants.BOOTCAMP_ALREADY_EXISTS)))
                .onErrorMap(OptimisticLockingFailureException.class, ex -> new BootcampAlreadyExistsException(
                        FunctionalMessageConstants.BUSINESS_VALIDATION_FAILED,
                        Map.of(FieldConstants.NAME, FunctionalMessageConstants.BOOTCAMP_ALREADY_EXISTS)));
    }

    @Override
    public Mono<Bootcamp> findByName(String name) {
        return repository.findByNameIgnoreCase(name).map(this::toEntity);
    }

    @Override
    public Mono<List<Bootcamp>> findPage(int page, int size, BootcampSortFieldEnum sortField, SortDirectionEnum direction) {
        long offset = (long) page * size;
        Flux<BootcampEntity> entities = switch (sortField) {
            case NAME -> direction == SortDirectionEnum.DESC
                    ? repository.findPageByNameDesc(size, offset)
                    : repository.findPageByNameAsc(size, offset);
            case CAPABILITY_COUNT -> direction == SortDirectionEnum.DESC
                    ? repository.findPageByCapabilityCountDesc(size, offset)
                    : repository.findPageByCapabilityCountAsc(size, offset);
        };

        return entities.map(this::toEntity).collectList();
    }

    @Override
    public Mono<Long> count() {
        Query completeOnly = Query.query(Criteria.where(STATUS_COLUMN).is(BootcampReactiveRepository.COMPLETE_STATUS));
        return template.count(completeOnly, BootcampEntity.class);
    }
}
