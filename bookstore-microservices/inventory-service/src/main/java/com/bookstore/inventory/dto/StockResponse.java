package com.bookstore.inventory.dto;

import java.time.LocalDateTime;

public record StockResponse(
        Long bookId,
        int quantity,
        LocalDateTime updatedAt
) {}