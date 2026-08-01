package co.com.pragma.events.dto;

import java.time.LocalDate;

public record BootcampReportEventDto(Long bootcampId, String name, String description, LocalDate launchDate,
        Integer durationInWeeks, Integer capabilityCount, Integer technologyCount, Integer enrolledPersonCount) {
}
