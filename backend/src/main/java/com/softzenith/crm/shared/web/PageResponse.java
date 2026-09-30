package com.softzenith.crm.shared.web;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Stable JSON shape for paged lists. */
public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<List<E>, List<T>> mapper) {
        return new PageResponse<>(mapper.apply(page.getContent()), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
