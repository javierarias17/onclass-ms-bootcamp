package co.com.pragma.model.bootcamp.command;

import java.time.LocalDate;
import java.util.List;

public record BootcampCreateCommand(String name, String description, LocalDate launchDate,
        Integer durationInWeeks, List<Long> capabilityIds) {
}
