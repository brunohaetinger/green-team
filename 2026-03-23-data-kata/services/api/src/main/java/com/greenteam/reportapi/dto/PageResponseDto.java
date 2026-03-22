package com.greenteam.reportapi.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        PageableDto pageable,
        long totalElements,
        int totalPages,
        boolean last
) {

    public record PageableDto(int pageNumber, int pageSize) {}

    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                new PageableDto(page.getNumber(), page.getSize()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
