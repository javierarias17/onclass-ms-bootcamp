package co.com.pragma.model.bootcamp.gateways;

import co.com.pragma.model.bootcamp.Bootcamp;
import reactor.core.publisher.Mono;

public interface BootcampRepository {

    Mono<Bootcamp> save(Bootcamp bootcamp);

    Mono<Bootcamp> findByName(String name);
}
