package com.bookstore.order.dto;

import com.bookstore.order.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        String shippingAddress,

        @NotNull(message = "Phương thức thanh toán không được để trống")
        PaymentMethod paymentMethod
) {}