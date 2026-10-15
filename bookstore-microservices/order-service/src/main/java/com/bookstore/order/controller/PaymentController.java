package com.bookstore.order.controller;

import com.bookstore.order.dto.CreatePaymentRequest;
import com.bookstore.order.dto.PaymentCallbackRequest;
import com.bookstore.order.dto.PaymentResponse;
import com.bookstore.order.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/orders/{orderId}/payments")
    public PaymentResponse createPayment(@PathVariable Long orderId, @RequestParam Long userId,
                                         @RequestBody(required = false) CreatePaymentRequest req) {
        return paymentService.createPayment(userId, orderId, req == null ? null : req.method());
    }

    @GetMapping("/api/orders/{orderId}/payments")
    public List<PaymentResponse> getPayments(@PathVariable Long orderId) {
        return paymentService.getPayments(orderId);
    }

    /** Giả lập cổng thanh toán báo kết quả. Bản thật phải có chữ ký xác thực. */
    @PostMapping("/api/payments/callback")
    public PaymentResponse callback(@Valid @RequestBody PaymentCallbackRequest req) {
        return paymentService.handleCallback(req);
    }
}