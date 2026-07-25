package co.com.pragma.model.bootcamp.gateways;

import co.com.pragma.model.bootcamp.Bootcamp;
import co.com.pragma.model.bootcamp.query.BootcampSortFieldEnum;
import co.com.pragma.model.bootcamp.query.SortDirectionEnum;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BootcampRepository {

    Mono<Bootcamp> save(Bootcamp bootcamp);

    Mono<Bootcamp> findByName(String name);

    Mono<List<Bootcamp>> findPage(int page, int size, BootcampSortFieldEnum sortField, SortDirectionEnum direction);

    Mono<Long> count();
}
