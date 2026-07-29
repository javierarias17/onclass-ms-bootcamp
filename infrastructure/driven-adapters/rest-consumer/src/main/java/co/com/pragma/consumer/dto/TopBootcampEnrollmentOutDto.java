package co.com.pragma.consumer.dto;

import java.util.List;

public record TopBootcampEnrollmentOutDto(Long bootcampId, Long enrolledPersonCount, List<EnrolledPersonDto> persons) {
}
