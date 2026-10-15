package com.bookstore.order.client;

import com.bookstore.order.exception.BadRequestException;
import com.bookstore.order.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(name = "inventory.mock", havingValue = "false", matchIfMissing = true)
public class HttpInventoryClient implements InventoryClient {

    public record StockDto(Long bookId, int quantity) {}
    public record ItemDto(Long bookId, int quantity) {}
    public record DeductBody(List<ItemDto> items) {}

    private final RestClient restClient;

    public HttpInventoryClient(@Value("${inventory.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public void checkStock(Long bookId, int quantity) {
        StockDto stock = call(() -> restClient.get()
                .uri("/api/inventory/{id}", bookId).retrieve().body(StockDto.class));
        int available = (stock == null) ? 0 : stock.quantity();
        if (available < quantity) {
            throw new BadRequestException("Sách " + bookId + " chỉ còn " + available + " cuốn trong kho");
        }
    }

    @Override
    public void deduct(Long orderId, List<StockItem> items) {
        DeductBody body = new DeductBody(
                items.stream().map(i -> new ItemDto(i.bookId(), i.quantity())).toList());
        call(() -> restClient.post()
                .uri("/api/inventory/orders/{id}/deduct", orderId)
                .contentType(MediaType.APPLICATION_JSON).body(body)
                .retrieve().toBodilessEntity());
    }

    @Override
    public void restore(Long orderId, List<StockItem> items) {
        // inventory-service tự hoàn đúng số đã trừ của đơn, nên không cần gửi items
        call(() -> restClient.post()
                .uri("/api/inventory/orders/{id}/restore", orderId)
                .retrieve().toBodilessEntity());
    }

    private <T> T call(Supplier<T> action) {
        try {
            return action.get();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400) {
                throw new BadRequestException(extractMessage(e));
            }
            throw new ServiceUnavailableException("inventory-service trả lỗi " + e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("Không kết nối được inventory-service");
        } catch (RestClientException e) {
            throw new ServiceUnavailableException("inventory-service đang gặp sự cố");
        }
    }

    private String extractMessage(HttpClientErrorException e) {
        try {
            Map<?, ?> body = e.getResponseBodyAs(Map.class);
            if (body != null && body.get("message") != null) return body.get("message").toString();
        } catch (Exception ignored) {
            // dùng thông báo mặc định bên dưới
        }
        return "Yêu cầu tồn kho không hợp lệ";
    }
}