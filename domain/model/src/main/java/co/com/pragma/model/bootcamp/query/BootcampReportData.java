package co.com.pragma.model.bootcamp.query;

import java.time.LocalDate;

public record BootcampReportData(Long bootcampId, String name, String description, LocalDate launchDate,
        Integer durationInWeeks, Integer capabilityCount, Integer technologyCount, Integer enrolledPersonCount) {
}
