package co.com.pragma.api;

import co.com.pragma.api.constants.PathVariableConstants;
import co.com.pragma.api.constants.QueryParamConstants;
import co.com.pragma.api.dto.BootcampInDto;
import co.com.pragma.api.dto.BootcampOutDto;
import co.com.pragma.api.dto.BootcampPageOutDto;
import co.com.pragma.api.dto.BootcampSchedulesInDto;
import co.com.pragma.api.dto.BootcampSchedulesOutDto;
import co.com.pragma.api.dto.TopBootcampOutDto;
import co.com.pragma.model.bootcamp.query.BootcampSortFieldEnum;
import co.com.pragma.model.bootcamp.query.SortDirectionEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

    @Operation(
            operationId = "listenListBootcamps",
            summary = "List bootcamps",
            description = "Returns a paginated list of bootcamps, each with its associated capabilities "
                    + "(id and name only) and the distinct set of technologies covered across all of them "
                    + "(id and name only, deduplicated).",
            tags = { "Bootcamps" },
            parameters = {
                    @Parameter(name = QueryParamConstants.PAGE, in = ParameterIn.QUERY, required = false,
                            schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = QueryParamConstants.SIZE, in = ParameterIn.QUERY, required = false,
                            schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = QueryParamConstants.SORT_BY, in = ParameterIn.QUERY, required = false,
                            schema = @Schema(implementation = BootcampSortFieldEnum.class, defaultValue = "NAME")),
                    @Parameter(name = QueryParamConstants.SORT_DIRECTION, in = ParameterIn.QUERY, required = false,
                            schema = @Schema(implementation = SortDirectionEnum.class, defaultValue = "ASC"))
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BootcampPageOutDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "content": [
                                        {
                                          "id": 1,
                                          "name": "Java Backend Bootcamp",
                                          "description": "Bootcamp de backend con Java",
                                          "launchDate": "2026-08-01",
                                          "durationInWeeks": 12,
                                          "capabilities": [
                                            { "id": 1, "name": "Backend" },
                                            { "id": 2, "name": "DevOps" }
                                          ],
                                          "technologies": [
                                            { "id": 10, "name": "Java" },
                                            { "id": 11, "name": "Spring" },
                                            { "id": 12, "name": "Docker" }
                                          ]
                                        }
                                      ],
                                      "page": 0,
                                      "size": 10,
                                      "totalElements": 1,
                                      "totalPages": 1
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "Business validation failed",
                                      "errors": [
                                        {
                                          "field": "sortBy",
                                          "message": "Sort field must be one of: NAME, CAPABILITY_COUNT"
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
    Mono<ServerResponse> listenListBootcamps(ServerRequest serverRequest);

    @Operation(
            operationId = "listenDeleteBootcamp",
            summary = "Delete a bootcamp",
            description = "Deletes a bootcamp and, cascading through capability-ms and technology-ms, any "
                    + "capability or technology left orphaned by its removal. The bootcamp is first marked as "
                    + "DELETING so that, if the cascade fails partway through, a background retry job can "
                    + "resume and complete it without requiring administrator intervention. "
                    + "Idempotent: calling it again once the bootcamp is already gone is a no-op.",
            tags = { "Bootcamps" },
            parameters = @Parameter(name = PathVariableConstants.ID, in = ParameterIn.PATH, required = true,
                    schema = @Schema(type = "integer", format = "int64")))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "Business validation failed",
                                      "errors": [
                                        {
                                          "field": "id",
                                          "message": "Bootcamp id must be numeric"
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "Business validation failed",
                                      "errors": [
                                        {
                                          "field": "id",
                                          "message": "Bootcamp not found"
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
    Mono<ServerResponse> listenDeleteBootcamp(ServerRequest serverRequest);

    @Operation(
            operationId = "listenFindBootcampSchedules",
            summary = "Find bootcamp schedules",
            description = "Given a list of bootcamp ids, returns the launch date and duration in weeks of each "
                    + "one that corresponds to an existing, fully created bootcamp (ids that don't match are "
                    + "silently omitted from the response). Used by other microservices (e.g. person-ms for "
                    + "HU-07 enrollment) to validate that a bootcampId exists and to check for schedule overlaps "
                    + "against bootcamps the person is already enrolled in.",
            tags = { "Bootcamps" },
            requestBody = @RequestBody(
                    description = "Input data",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BootcampSchedulesInDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "bootcampIds": [1, 99]
                                    }
                                    """))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BootcampSchedulesOutDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "schedules": [
                                        { "id": 1, "launchDate": "2026-08-01", "durationInWeeks": 12 }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "Business validation failed",
                                      "errors": [
                                        {
                                          "field": "bootcampIds",
                                          "message": "Bootcamp ids list is required and must not be empty"
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
                                    """)))
    })
    Mono<ServerResponse> listenFindBootcampSchedules(ServerRequest serverRequest);

    @Operation(
            operationId = "listenFindTopBootcamp",
            summary = "Find the bootcamp with the most enrolled persons",
            description = "Returns the full detail of the bootcamp with the highest number of enrolled persons: "
                    + "its own data, every capability and technology associated with it, and the name and email "
                    + "of each enrolled person. Ties are broken by the lowest bootcamp id. If no bootcamp has any "
                    + "enrollments yet, responds 204 with no body (never 404 — that status is reserved for "
                    + "the resource path itself not existing).",
            tags = { "Bootcamps" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TopBootcampOutDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "name": "Java Backend Bootcamp",
                                      "description": "Bootcamp de backend con Java",
                                      "launchDate": "2026-08-01",
                                      "durationInWeeks": 12,
                                      "capabilities": [
                                        { "id": 1, "name": "Backend" }
                                      ],
                                      "technologies": [
                                        { "id": 10, "name": "Java" }
                                      ],
                                      "enrolledPersonCount": 2,
                                      "persons": [
                                        { "name": "Ada Lovelace", "email": "ada@mail.com" },
                                        { "name": "Alan Turing", "email": "alan@mail.com" }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "204", description = "No Content. No bootcamp has any enrollments yet."),
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
    Mono<ServerResponse> listenFindTopBootcamp(ServerRequest serverRequest);
}
