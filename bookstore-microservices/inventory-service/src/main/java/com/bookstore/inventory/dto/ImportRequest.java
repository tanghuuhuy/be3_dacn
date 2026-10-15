package com.bookstore.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ImportRequest(
        @NotNull Long bookId,
        @Min(1) int quantity,
        String note
) {}