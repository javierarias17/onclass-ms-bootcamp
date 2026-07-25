package co.com.pragma.api.dto;

import java.util.List;

public record BootcampPageOutDto(List<BootcampListItemOutDto> content, int page, int size, long totalElements,
        int totalPages) {
}
