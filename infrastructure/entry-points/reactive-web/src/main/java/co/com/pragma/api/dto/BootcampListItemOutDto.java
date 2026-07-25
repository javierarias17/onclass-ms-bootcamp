package co.com.pragma.api.dto;

import java.time.LocalDate;
import java.util.List;

public record BootcampListItemOutDto(Long id, String name, String description, LocalDate launchDate,
        Integer durationInWeeks, List<CapabilitySummaryOutDto> capabilities,
        List<TechnologySummaryOutDto> technologies) {
}
