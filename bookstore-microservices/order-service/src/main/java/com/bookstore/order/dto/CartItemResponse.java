package com.bookstore.order.dto;

import java.math.BigDecimal;

public record CartItemResponse(Long bookId, String title, BigDecimal unitPrice,
                               int quantity, BigDecimal lineTotal) {}