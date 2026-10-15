package com.bookstore.order.client;

import java.util.List;

public interface InventoryClient {
    void checkStock(Long bookId, int quantity);          // không đủ hàng thì ném BadRequestException
    void deduct(Long orderId, List<StockItem> items);    // trừ kho, hoặc trừ hết hoặc không trừ gì
    void restore(Long orderId, List<StockItem> items);   // hoàn kho
}