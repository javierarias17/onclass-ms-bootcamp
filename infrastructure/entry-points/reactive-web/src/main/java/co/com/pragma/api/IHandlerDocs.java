package co.com.pragma.api;

import co.com.pragma.api.dto.BootcampInDto;
import co.com.pragma.api.dto.BootcampOutDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface IHandlerDocs {

    @Operation(
            operationId = "listenRegisterBootcamp",
            summary = "Register a bootcamp",
            description = "Creates a new bootcamp with a unique name and between 1 and 4 existing capability ids.",
            tags = { "Bootcamps" },
            requestBody = @RequestBody(
                    description = "Input data",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BootcampInDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Java Backend Bootcamp",
                                      "description": "Bootcamp de backend con Java",
                                      "launchDate": "2026-08-01",
                                      "durationInWeeks": 12,
                                      "capabilityIds": [1, 2]
                                    }
                                    """))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BootcampOutDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "name": "Java Backend Bootcamp",
                                      "description": "Bootcamp de backend con Java",
                                      "launchDate": "2026-08-01",
                                      "durationInWeeks": 12,
                                      "capabilityIds": [1, 2]
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Invalid input", value = """
                                            {
                                              "message": "Business validation failed",
                                              "errors": [
                                                {
                                                  "field": "name",
                                                  "message": "Bootcamp name is required"
                                                },
                                                {
                                                  "field": "capabilityIds",
                                                  "message": "Bootcamp must have between 1 and 4 capabilities"
                                                }
                                              ]
                                            }
                                            """),
                                    @ExampleObject(name = "Duplicated capability ids", value = """
                                            {
                                              "message": "Business validation failed",
                                              "errors": [
                                                {
                                                  "field": "capabilityIds",
                                                  "message": "Bootcamp capability ids must not contain duplicates"
                                                }
                                              ]
                                            }
                                            """),
                                    @ExampleObject(name = "Capabilities not found", value = """
                                            {
                                              "message": "Business validation failed",
                                              "errors": [
                                                {
                                                  "field": "capabilityIds",
                                                  "message": "The following capability ids do not exist: [99]"
                                                }
                                              ]
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "Business validation failed",
                                      "errors": [
                                        {
                                          "field": "name",
                                          "message": "Bootcamp name already exists"
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "An unexpected error occurred. Please contact the administrator."
                                    }
                                    """))),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "The service is temporarily unavailable. Please try again shortly."
                                    }
                                    """)))
    })
    Mono<ServerResponse> listenRegisterBootcamp(ServerRequest serverRequest);
}
