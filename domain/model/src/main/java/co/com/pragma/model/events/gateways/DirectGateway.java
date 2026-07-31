package co.com.pragma.model.events.gateways;

import reactor.core.publisher.Mono;

public interface DirectGateway {
    Mono<Void> sendCommand(Object event);
    Mono<Object/*change for proper model*/> requestReply(Object event);
}
