package com.bookstore.inventory.dto;

import com.bookstore.inventory.enums.InventoryTxType;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long bookId,
        InventoryTxType type,
        int quantityChange,
        int quantityAfter,
        Long orderId,
        String note,
        LocalDateTime createdAt
) {}