package com.bookstore.order.dto;

import com.bookstore.order.enums.PaymentMethod;
import com.bookstore.order.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        PaymentMethod method,
        BigDecimal amount,
        PaymentStatus status,
        String transactionRef,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        String instruction
) {}