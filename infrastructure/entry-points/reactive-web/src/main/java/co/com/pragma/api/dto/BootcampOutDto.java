package co.com.pragma.api.dto;

import java.time.LocalDate;
import java.util.List;

public record BootcampOutDto(Long id, String name, String description, LocalDate launchDate,
        Integer durationInWeeks, List<Long> capabilityIds) {
}
