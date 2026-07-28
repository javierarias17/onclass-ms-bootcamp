package co.com.pragma.api.dto;

import java.time.LocalDate;

public record BootcampScheduleOutDto(Long id, LocalDate launchDate, Integer durationInWeeks) {
}
