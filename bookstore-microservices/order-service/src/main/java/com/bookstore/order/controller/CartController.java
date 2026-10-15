package com.bookstore.order.controller;

import com.bookstore.order.dto.*;
import com.bookstore.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@RequestParam Long userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    public CartResponse addItem(@RequestParam Long userId, @Valid @RequestBody CartItemRequest req) {
        return cartService.addItem(userId, req);
    }

    @PutMapping("/items/{bookId}")
    public CartResponse updateQuantity(@RequestParam Long userId, @PathVariable Long bookId,
                                       @Valid @RequestBody UpdateQuantityRequest req) {
        return cartService.updateQuantity(userId, bookId, req.quantity());
    }

    @DeleteMapping("/items/{bookId}")
    public CartResponse removeItem(@RequestParam Long userId, @PathVariable Long bookId) {
        return cartService.removeItem(userId, bookId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestParam Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}