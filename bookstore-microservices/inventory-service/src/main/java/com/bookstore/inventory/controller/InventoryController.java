package com.bookstore.inventory.controller;

import com.bookstore.inventory.dto.*;
import com.bookstore.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<StockResponse> getAll() {
        return inventoryService.getAll();
    }

    @GetMapping("/{bookId}")
    public StockResponse getStock(@PathVariable Long bookId) {
        return inventoryService.getStock(bookId);
    }

    @GetMapping("/low-stock")
    public List<StockResponse> lowStock(@RequestParam(defaultValue = "5") int threshold) {
        return inventoryService.getLowStock(threshold);
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> transactions(@RequestParam(required = false) Long bookId) {
        return inventoryService.getTransactions(bookId);
    }

    @PostMapping("/import")
    public StockResponse importStock(@Valid @RequestBody ImportRequest req) {
        return inventoryService.importStock(req);
    }

    @PostMapping("/adjust")
    public StockResponse adjust(@Valid @RequestBody AdjustRequest req) {
        return inventoryService.adjust(req);
    }

    @PostMapping("/orders/{orderId}/deduct")
    public ResponseEntity<Void> deduct(@PathVariable Long orderId, @Valid @RequestBody DeductRequest req) {
        inventoryService.deduct(orderId, req.items());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/{orderId}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long orderId) {
        inventoryService.restore(orderId);
        return ResponseEntity.noContent().build();
    }
}