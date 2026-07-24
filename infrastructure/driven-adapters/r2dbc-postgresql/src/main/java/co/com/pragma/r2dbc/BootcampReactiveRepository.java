package co.com.pragma.r2dbc;

import co.com.pragma.r2dbc.entity.BootcampEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BootcampReactiveRepository extends
        ReactiveCrudRepository<BootcampEntity, Long>,
        ReactiveQueryByExampleExecutor<BootcampEntity> {

    Mono<BootcampEntity> findByNameIgnoreCase(String name);
}
