package com.bookstore.order.dto;

import com.bookstore.order.enums.PaymentMethod;

public record CreatePaymentRequest(
        PaymentMethod method
) {}