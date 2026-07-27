package co.com.pragma.scheduler;

import co.com.pragma.usecase.retrypendingbootcampdeletions.RetryPendingBootcampDeletionsUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BootcampDeletionRetryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BootcampDeletionRetryScheduler.class);

    private final RetryPendingBootcampDeletionsUseCase retryPendingBootcampDeletionsUseCase;

    @Scheduled(fixedDelayString = "${scheduler.bootcamp-deletion-retry.fixed-delay:60000}")
    public void retryPendingDeletions() {
        retryPendingBootcampDeletionsUseCase.execute()
                .onErrorResume(error -> {
                    logger.error("Error retrying pending bootcamp deletions", error);
                    return Mono.empty();
                })
                .subscribe();
    }
}
