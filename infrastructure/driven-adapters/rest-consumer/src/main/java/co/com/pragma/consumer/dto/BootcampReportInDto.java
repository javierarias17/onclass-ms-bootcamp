package co.com.pragma.consumer.dto;

import java.time.LocalDate;

public record BootcampReportInDto(Long bootcampId, String name, String description, LocalDate launchDate,
        Integer durationInWeeks, Integer capabilityCount, Integer technologyCount, Integer enrolledPersonCount) {
}
