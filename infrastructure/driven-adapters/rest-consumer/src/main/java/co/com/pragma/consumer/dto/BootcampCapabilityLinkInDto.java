package co.com.pragma.consumer.dto;

import java.util.List;

public record BootcampCapabilityLinkInDto(Long bootcampId, List<Long> capabilityIds) {
}
