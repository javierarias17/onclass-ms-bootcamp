package co.com.pragma.model.bootcamp.query;

import java.util.List;

public record TopBootcampEnrollmentSummary(Long bootcampId, Long enrolledPersonCount,
        List<EnrolledPersonSummary> persons) {
}
