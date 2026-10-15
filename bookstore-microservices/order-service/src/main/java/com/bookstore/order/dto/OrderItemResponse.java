package com.bookstore.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long bookId,
        String bookTitle,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {}