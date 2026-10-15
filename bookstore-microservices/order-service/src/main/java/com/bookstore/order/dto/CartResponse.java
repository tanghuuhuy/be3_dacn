package com.bookstore.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long cartId, List<CartItemResponse> items, BigDecimal totalAmount) {}