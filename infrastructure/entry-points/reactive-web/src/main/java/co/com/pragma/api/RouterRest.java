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
    private static final String BOOTCAMPS_SCHEDULES_PATH = BOOTCAMPS_PATH + "/schedules";

    @Bean
    @RouterOperations({
            @RouterOperation(path = BOOTCAMPS_PATH, method = {
                    RequestMethod.POST }, beanClass = Handler.class, beanMethod = "listenRegisterBootcamp"),
            @RouterOperation(path = BOOTCAMPS_PATH, method = {
                    RequestMethod.GET }, beanClass = Handler.class, beanMethod = "listenListBootcamps"),
            @RouterOperation(path = BOOTCAMP_BY_ID_PATH, method = {
                    RequestMethod.DELETE }, beanClass = Handler.class, beanMethod = "listenDeleteBootcamp"),
            @RouterOperation(path = BOOTCAMPS_SCHEDULES_PATH, method = {
                    RequestMethod.POST }, beanClass = Handler.class, beanMethod = "listenFindBootcampSchedules")
    })
    public RouterFunction<ServerResponse> bootcampRouterFunction(Handler handler) {
        //HU-04
        return route(POST(BOOTCAMPS_PATH), handler::listenRegisterBootcamp)
                //HU-05
                .andRoute(GET(BOOTCAMPS_PATH), handler::listenListBootcamps)
                //HU-06
                .andRoute(DELETE(BOOTCAMP_BY_ID_PATH), handler::listenDeleteBootcamp)
                //HU-07
                .andRoute(POST(BOOTCAMPS_SCHEDULES_PATH), handler::listenFindBootcampSchedules);
    }
}
