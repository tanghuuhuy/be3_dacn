package com.bookstore.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockItemRequest(
        @NotNull Long bookId,
        @Min(1) int quantity
) {}