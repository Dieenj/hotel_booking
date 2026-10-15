package com.hotelbooking.common.response;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/** Builds page requests from client input, clamped so a caller cannot ask for unbounded pages. */
public final class Pagination {

    public static final int MAX_PAGE_SIZE = 100;

    private Pagination() {
    }

    public static PageRequest of(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE), sort);
    }
}
