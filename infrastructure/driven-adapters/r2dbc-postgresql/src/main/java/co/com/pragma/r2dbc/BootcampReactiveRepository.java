package co.com.pragma.r2dbc;

import co.com.pragma.r2dbc.entity.BootcampEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BootcampReactiveRepository extends
        ReactiveCrudRepository<BootcampEntity, Long>,
        ReactiveQueryByExampleExecutor<BootcampEntity> {

    String CREATED_STATUS = "CREATED";
    String DELETING_STATUS = "DELETING";

    Mono<BootcampEntity> findByNameIgnoreCase(String name);

    @Query("SELECT * FROM bootcamps WHERE status = '" + DELETING_STATUS + "'")
    Flux<BootcampEntity> findAllByStatusDeleting();

    @Query("SELECT * FROM bootcamps WHERE status = '" + CREATED_STATUS
            + "' ORDER BY LOWER(name) ASC LIMIT :size OFFSET :offset")
    Flux<BootcampEntity> findPageByNameAsc(@Param("size") int size, @Param("offset") long offset);

    @Query("SELECT * FROM bootcamps WHERE status = '" + CREATED_STATUS
            + "' ORDER BY LOWER(name) DESC LIMIT :size OFFSET :offset")
    Flux<BootcampEntity> findPageByNameDesc(@Param("size") int size, @Param("offset") long offset);

    @Query("SELECT * FROM bootcamps WHERE status = '" + CREATED_STATUS
            + "' ORDER BY capability_count ASC LIMIT :size OFFSET :offset")
    Flux<BootcampEntity> findPageByCapabilityCountAsc(@Param("size") int size, @Param("offset") long offset);

    @Query("SELECT * FROM bootcamps WHERE status = '" + CREATED_STATUS
            + "' ORDER BY capability_count DESC LIMIT :size OFFSET :offset")
    Flux<BootcampEntity> findPageByCapabilityCountDesc(@Param("size") int size, @Param("offset") long offset);

    @Query("SELECT * FROM bootcamps WHERE id IN (:bootcampIds) AND status = '" + CREATED_STATUS + "'")
    Flux<BootcampEntity> findByIdIn(@Param("bootcampIds") List<Long> bootcampIds);
}
