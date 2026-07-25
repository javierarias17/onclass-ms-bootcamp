package co.com.pragma.consumer.dto;

import java.util.List;

public record CapabilitiesByBootcampEntryDto(Long bootcampId, List<CapabilitySummaryDto> capabilities) {
}
