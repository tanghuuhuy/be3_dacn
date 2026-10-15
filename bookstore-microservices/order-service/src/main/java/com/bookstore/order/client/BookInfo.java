package com.bookstore.order.client;

import java.math.BigDecimal;

public record BookInfo(Long id, String title, BigDecimal price) {}