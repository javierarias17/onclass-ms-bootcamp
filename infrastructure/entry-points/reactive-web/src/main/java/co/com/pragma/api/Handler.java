package co.com.pragma.api;

import co.com.pragma.api.constants.PathVariableConstants;
import co.com.pragma.api.constants.QueryParamConstants;
import co.com.pragma.api.dto.BootcampInDto;
import co.com.pragma.api.dto.BootcampSchedulesInDto;
import co.com.pragma.api.mapper.BootcampDtoMapper;
import co.com.pragma.model.bootcamp.query.BootcampListQuery;
import co.com.pragma.model.bootcamp.query.BootcampSortFieldEnum;
import co.com.pragma.model.bootcamp.query.SortDirectionEnum;
import co.com.pragma.usecase.deletebootcamp.DeleteBootcampUseCase;
import co.com.pragma.usecase.findbootcampschedules.FindBootcampSchedulesUseCase;
import co.com.pragma.usecase.findtopbootcamp.FindTopBootcampUseCase;
import co.com.pragma.usecase.listbootcamps.ListBootcampsUseCase;
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

    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_SIZE = "10";

    private final RegisterBootcampUseCase registerBootcampUseCase;
    private final ListBootcampsUseCase listBootcampsUseCase;
    private final DeleteBootcampUseCase deleteBootcampUseCase;
    private final FindBootcampSchedulesUseCase findBootcampSchedulesUseCase;
    private final FindTopBootcampUseCase findTopBootcampUseCase;
    private final BootcampDtoMapper bootcampDtoMapper;

    @Override
    public Mono<ServerResponse> listenRegisterBootcamp(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(BootcampInDto.class)
                .defaultIfEmpty(BootcampInDto.builder().build())
                .map(bootcampDtoMapper::toBootcampCreateCommand)
                .flatMap(command -> registerBootcampUseCase.execute(command)
                        .map(bootcamp -> bootcampDtoMapper.toBootcampOutDto(bootcamp, command.capabilityIds())))
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    @Override
    public Mono<ServerResponse> listenListBootcamps(ServerRequest serverRequest) {
        BootcampListQuery query = new BootcampListQuery(
                serverRequest.queryParam(QueryParamConstants.PAGE).orElse(DEFAULT_PAGE),
                serverRequest.queryParam(QueryParamConstants.SIZE).orElse(DEFAULT_SIZE),
                serverRequest.queryParam(QueryParamConstants.SORT_BY).orElse(BootcampSortFieldEnum.NAME.name()),
                serverRequest.queryParam(QueryParamConstants.SORT_DIRECTION).orElse(SortDirectionEnum.ASC.name()));

        return listBootcampsUseCase.execute(query)
                .map(bootcampDtoMapper::toBootcampPageOutDto)
                .flatMap(response -> ServerResponse.status(HttpStatus.OK).bodyValue(response));
    }

    @Override
    public Mono<ServerResponse> listenDeleteBootcamp(ServerRequest serverRequest) {
        return Mono.just(serverRequest.pathVariable(PathVariableConstants.ID))
                .flatMap(deleteBootcampUseCase::execute)
                .then(ServerResponse.noContent().build());
    }

    @Override
    public Mono<ServerResponse> listenFindBootcampSchedules(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(BootcampSchedulesInDto.class)
                .defaultIfEmpty(BootcampSchedulesInDto.builder().build())
                .flatMap(dto -> findBootcampSchedulesUseCase.execute(dto.bootcampIds()))
                .map(bootcampDtoMapper::toBootcampSchedulesOutDto)
                .flatMap(response -> ServerResponse.status(HttpStatus.OK).bodyValue(response));
    }

    @Override
    public Mono<ServerResponse> listenFindTopBootcamp(ServerRequest serverRequest) {
        return findTopBootcampUseCase.execute()
                .map(bootcampDtoMapper::toTopBootcampOutDto)
                .flatMap(response -> ServerResponse.status(HttpStatus.OK).bodyValue(response))
                .switchIfEmpty(ServerResponse.noContent().build());
    }
}
