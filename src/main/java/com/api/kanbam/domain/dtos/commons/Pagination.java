package com.api.kanbam.domain.dtos.commons;

import java.util.List;

public record Pagination<T>(
        List<T> content,
        int page,
        int totalPage,
        long totalElements
) {
}
