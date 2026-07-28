package co.com.pragma.api.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record BootcampInDto(String name, String description, LocalDate launchDate,
        Integer durationInWeeks, List<Long> capabilityIds) {
}
