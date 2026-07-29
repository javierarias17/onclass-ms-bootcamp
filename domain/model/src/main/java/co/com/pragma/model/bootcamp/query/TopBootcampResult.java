package co.com.pragma.model.bootcamp.query;

import co.com.pragma.model.bootcamp.Bootcamp;

import java.util.List;

public record TopBootcampResult(Bootcamp bootcamp, List<CapabilitySummary> capabilities,
        List<TechnologySummary> technologies, Long enrolledPersonCount, List<EnrolledPersonSummary> persons) {
}
