package com.upi.auth_service.dto;

import lombok.*;

import java.util.List;

/**
 * Generic paginated response wrapper used instead of leaking Spring's {@code Page<T>} directly.
 * Keeps the API contract stable even if the persistence library changes.
 *
 * @param <T> the content element type
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean last;
}
