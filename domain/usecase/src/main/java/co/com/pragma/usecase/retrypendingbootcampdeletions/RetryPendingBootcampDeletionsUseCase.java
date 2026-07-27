package co.com.pragma.usecase.retrypendingbootcampdeletions;

import co.com.pragma.model.bootcamp.gateways.BootcampRepository;
import co.com.pragma.usecase.deletebootcamp.DeleteBootcampUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RetryPendingBootcampDeletionsUseCase {

    private final BootcampRepository bootcampRepository;
    private final DeleteBootcampUseCase deleteBootcampUseCase;

    public Mono<Void> execute() {
        return bootcampRepository.findAllPendingDeletion()
                .flatMapMany(Flux::fromIterable)
                .flatMap(deleteBootcampUseCase::resumeCascade)
                .then();
    }
}
