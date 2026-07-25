package co.com.pragma.consumer.dto;

import java.util.List;

public record CapabilitySummaryDto(Long id, String name, List<TechnologySummaryDto> technologies) {
}
