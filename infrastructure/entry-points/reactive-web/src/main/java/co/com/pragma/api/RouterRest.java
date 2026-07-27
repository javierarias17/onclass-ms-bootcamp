package co.com.pragma.api;

import co.com.pragma.api.constants.PathVariableConstants;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    private static final String BOOTCAMPS_PATH = "/api/v1/bootcamps";
    private static final String BOOTCAMP_BY_ID_PATH = BOOTCAMPS_PATH + "/{" + PathVariableConstants.ID + "}";

    @Bean
    @RouterOperations({
            @RouterOperation(path = BOOTCAMPS_PATH, method = {
                    RequestMethod.POST }, beanClass = Handler.class, beanMethod = "listenRegisterBootcamp"),
            @RouterOperation(path = BOOTCAMPS_PATH, method = {
                    RequestMethod.GET }, beanClass = Handler.class, beanMethod = "listenListBootcamps"),
            @RouterOperation(path = BOOTCAMP_BY_ID_PATH, method = {
                    RequestMethod.DELETE }, beanClass = Handler.class, beanMethod = "listenDeleteBootcamp")
    })
    public RouterFunction<ServerResponse> bootcampRouterFunction(Handler handler) {
        return route(POST(BOOTCAMPS_PATH), handler::listenRegisterBootcamp)
                .andRoute(GET(BOOTCAMPS_PATH), handler::listenListBootcamps)
                .andRoute(DELETE(BOOTCAMP_BY_ID_PATH), handler::listenDeleteBootcamp);
    }
}
