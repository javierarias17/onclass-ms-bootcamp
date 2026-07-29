package co.com.pragma.model.bootcamp.gateways;

import co.com.pragma.model.bootcamp.query.TopBootcampEnrollmentSummary;
import reactor.core.publisher.Mono;

public interface PersonGateway {

    Mono<TopBootcampEnrollmentSummary> findTopBootcampEnrollment();
}
