package com.bookstore.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdjustRequest(
        @NotNull Long bookId,
        @Min(0) int newQuantity,
        String note
) {}