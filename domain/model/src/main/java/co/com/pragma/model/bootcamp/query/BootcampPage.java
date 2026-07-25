package co.com.pragma.model.bootcamp.query;

import java.util.List;

public record BootcampPage(List<BootcampListItem> content, int page, int size, long totalElements,
        int totalPages) {
}
