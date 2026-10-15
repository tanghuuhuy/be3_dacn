package com.bookstore.order.client;

import com.bookstore.order.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@Profile("!mock")
public class HttpCatalogClient implements CatalogClient {

    private final RestClient restClient;

    public HttpCatalogClient(@Value("${catalog.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public BookInfo getBook(Long bookId) {
        try {
            return restClient.get().uri("/api/books/{id}", bookId)
                    .retrieve().body(BookInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Không tìm thấy sách " + bookId);
        }
    }
}