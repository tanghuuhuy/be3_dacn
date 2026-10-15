package com.bookstore.order.client;

import com.bookstore.order.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Profile("mock")
public class MockCatalogClient implements CatalogClient {

    private final Map<Long, BookInfo> books = Map.of(
            1L, new BookInfo(1L, "Dac Nhan Tam", new BigDecimal("86000")),
            2L, new BookInfo(2L, "Nha Gia Kim", new BigDecimal("79000")));

    @Override
    public BookInfo getBook(Long bookId) {
        BookInfo book = books.get(bookId);
        if (book == null) throw new ResourceNotFoundException("Không tìm thấy sách " + bookId);
        return book;
    }
}