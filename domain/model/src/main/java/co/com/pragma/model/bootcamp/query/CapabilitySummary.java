package co.com.pragma.model.bootcamp.query;

import java.util.List;

public record CapabilitySummary(Long id, String name, List<TechnologySummary> technologies) {
}
