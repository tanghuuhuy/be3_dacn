package com.bookstore.order.controller;

import com.bookstore.order.dto.*;
import com.bookstore.order.enums.OrderStatus;
import com.bookstore.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponse placeOrder(@RequestParam Long userId,
                                    @Valid @RequestBody PlaceOrderRequest req) {
        return orderService.placeOrder(userId, req);
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders(@RequestParam Long userId) {
        return orderService.getMyOrders(userId);
    }

    @GetMapping
    public List<OrderResponse> allOrders(@RequestParam(required = false) OrderStatus status) {
        return orderService.getAllOrders(status);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody UpdateStatusRequest req) {
        return orderService.updateStatus(id, req.status());
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id, @RequestParam Long userId) {
        return orderService.cancelByCustomer(userId, id);
    }
}