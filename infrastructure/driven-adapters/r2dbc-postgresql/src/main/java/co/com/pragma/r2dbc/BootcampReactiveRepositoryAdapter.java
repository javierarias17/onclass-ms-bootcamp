package co.com.pragma.r2dbc;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.exceptions.BootcampAlreadyExistsException;
import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.model.common.FieldConstants;
import co.com.pragma.model.exceptions.constant.FunctionalMessageConstants;
import co.com.pragma.r2dbc.entity.BootcampEntity;
import co.com.pragma.r2dbc.helper.ReactiveAdapterOperations;
import co.com.pragma.r2dbc.mapper.BootcampEntityMapper;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;

@Repository
public class BootcampReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Bootcamp,
        BootcampEntity,
        Long,
        BootcampReactiveRepository
        > implements BootcampRepository {

    private final BootcampEntityMapper bootcampEntityMapper;

    public BootcampReactiveRepositoryAdapter(BootcampReactiveRepository repository, ObjectMapper mapper,
                                              BootcampEntityMapper bootcampEntityMapper) {
        super(repository, mapper, bootcampEntityMapper::toDomain);
        this.bootcampEntityMapper = bootcampEntityMapper;
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
}
