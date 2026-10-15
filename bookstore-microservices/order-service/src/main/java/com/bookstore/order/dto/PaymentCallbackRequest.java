package com.bookstore.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCallbackRequest(
        @NotBlank(message = "Mã giao dịch không được để trống")
        String transactionRef,

        @NotNull(message = "Trạng thái thành công không được để trống")
        Boolean success
) {}