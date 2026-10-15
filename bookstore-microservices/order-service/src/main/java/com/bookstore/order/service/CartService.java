package com.bookstore.order.service;

import com.bookstore.order.client.BookInfo;
import com.bookstore.order.client.CatalogClient;
import com.bookstore.order.dto.*;
import com.bookstore.order.entity.Cart;
import com.bookstore.order.entity.CartItem;
import com.bookstore.order.exception.ResourceNotFoundException;
import com.bookstore.order.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CatalogClient catalogClient;

    @Transactional
    public CartResponse getCart(Long userId) {
        return toResponse(getOrCreateCart(userId));
    }

    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest req) {
        Cart cart = getOrCreateCart(userId);
        catalogClient.getBook(req.bookId());   // ném 404 nếu sách không tồn tại

        CartItem item = findItem(cart, req.bookId());
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setBookId(req.bookId());
            item.setQuantity(req.quantity());
            cart.getItems().add(item);
        } else {
            item.setQuantity(item.getQuantity() + req.quantity());
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long bookId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findItem(cart, bookId);
        if (item == null) throw new ResourceNotFoundException("Sách không có trong giỏ hàng");
        item.setQuantity(quantity);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long bookId) {
        Cart cart = getOrCreateCart(userId);
        boolean removed = cart.getItems().removeIf(i -> i.getBookId().equals(bookId));
        if (!removed) throw new ResourceNotFoundException("Sách không có trong giỏ hàng");
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return cartRepository.save(cart);
        });
    }

    private CartItem findItem(Cart cart, Long bookId) {
        return cart.getItems().stream()
                .filter(i -> i.getBookId().equals(bookId))
                .findFirst().orElse(null);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(i -> {
            BookInfo book = catalogClient.getBook(i.getBookId());
            return new CartItemResponse(book.id(), book.title(), book.price(), i.getQuantity(),
                    book.price().multiply(BigDecimal.valueOf(i.getQuantity())));
        }).toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, total);
    }
}