package co.com.pragma.api.dto;

import java.time.LocalDate;
import java.util.List;

public record BootcampInDto(String name, String description, LocalDate launchDate,
        Integer durationInWeeks, List<Long> capabilityIds) {
}
