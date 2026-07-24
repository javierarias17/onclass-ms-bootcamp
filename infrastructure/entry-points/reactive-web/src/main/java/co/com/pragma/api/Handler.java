package co.com.pragma.api;

import co.com.pragma.api.dto.BootcampInDto;
import co.com.pragma.api.mapper.BootcampDtoMapper;
import co.com.pragma.usecase.registerbootcamp.RegisterBootcampUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler implements IHandlerDocs {

    private final RegisterBootcampUseCase registerBootcampUseCase;
    private final BootcampDtoMapper bootcampDtoMapper;

    @Override
    public Mono<ServerResponse> listenRegisterBootcamp(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(BootcampInDto.class)
                .defaultIfEmpty(new BootcampInDto(null, null, null, null, null))
                .map(bootcampDtoMapper::toBootcampCreateCommand)
                .flatMap(command -> registerBootcampUseCase.execute(command)
                        .map(bootcamp -> bootcampDtoMapper.toBootcampOutDto(bootcamp, command.capabilityIds())))
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }
}
