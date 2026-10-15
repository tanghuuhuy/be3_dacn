package com.bookstore.inventory.service;

import com.bookstore.inventory.dto.*;
import com.bookstore.inventory.entity.InventoryTransaction;
import com.bookstore.inventory.entity.Stock;
import com.bookstore.inventory.enums.InventoryTxType;
import com.bookstore.inventory.exception.BadRequestException;
import com.bookstore.inventory.repository.InventoryTransactionRepository;
import com.bookstore.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.bookstore.inventory.enums.InventoryTxType.*;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final StockRepository stockRepository;
    private final InventoryTransactionRepository txRepository;

    // ---------- đọc ----------

    @Transactional(readOnly = true)
    public StockResponse getStock(Long bookId) {
        return stockRepository.findById(bookId).map(this::toResponse)
                .orElseGet(() -> new StockResponse(bookId, 0, null));   // chưa nhập kho = 0
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getAll() {
        return stockRepository.findAll(Sort.by("bookId")).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getLowStock(int threshold) {
        return stockRepository.findByQuantityLessThanEqualOrderByQuantityAsc(threshold)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long bookId) {
        List<InventoryTransaction> list = (bookId == null)
                ? txRepository.findTop100ByOrderByCreatedAtDesc()
                : txRepository.findByBookIdOrderByCreatedAtDesc(bookId);
        return list.stream().map(this::toResponse).toList();
    }

    // ---------- nhập kho / kiểm kê ----------

    @Transactional
    public StockResponse importStock(ImportRequest req) {
        Stock stock = lockOrCreate(req.bookId());
        applyChange(stock, req.quantity());
        record(stock, IMPORT, req.quantity(), null, req.note());
        return toResponse(stock);
    }

    @Transactional
    public StockResponse adjust(AdjustRequest req) {
        Stock stock = lockOrCreate(req.bookId());
        int change = req.newQuantity() - stock.getQuantity();
        if (change == 0) return toResponse(stock);
        applyChange(stock, change);
        record(stock, ADJUST, change, null, req.note());
        return toResponse(stock);
    }

    // ---------- đơn hàng gọi vào ----------

    /** Trừ kho khi đơn được xác nhận: đủ hàng cho mọi dòng thì mới trừ, không thì không trừ gì. */
    @Transactional
    public void deduct(Long orderId, List<StockItemRequest> items) {
        if (txRepository.existsByOrderIdAndType(orderId, ORDER_DEDUCT)) return;   // gọi lặp lại: bỏ qua

        Map<Long, Integer> wanted = new TreeMap<>();                  // TreeMap: khóa theo thứ tự book_id
        for (StockItemRequest i : items) wanted.merge(i.bookId(), i.quantity(), Integer::sum);

        Map<Long, Stock> locked = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> e : wanted.entrySet()) {
            Stock stock = stockRepository.findByIdForUpdate(e.getKey()).orElse(null);
            int available = (stock == null) ? 0 : stock.getQuantity();
            if (available < e.getValue()) {
                throw new BadRequestException("Sách " + e.getKey() + " chỉ còn " + available + " cuốn trong kho");
            }
            locked.put(e.getKey(), stock);
        }
        for (Map.Entry<Long, Integer> e : wanted.entrySet()) {
            Stock stock = locked.get(e.getKey());
            applyChange(stock, -e.getValue());
            record(stock, ORDER_DEDUCT, -e.getValue(), orderId, "Trừ kho khi xác nhận đơn");
        }
    }

    /** Hoàn kho khi đơn bị hủy hoặc trả: chỉ hoàn đúng số đã trừ, và chỉ một lần. */
    @Transactional
    public void restore(Long orderId) {
        List<InventoryTransaction> deducts = txRepository.findByOrderIdAndType(orderId, ORDER_DEDUCT);
        if (deducts.isEmpty() || txRepository.existsByOrderIdAndType(orderId, ORDER_RESTORE)) return;

        List<InventoryTransaction> sorted = deducts.stream()
                .sorted(Comparator.comparing(InventoryTransaction::getBookId)).toList();
        for (InventoryTransaction tx : sorted) {
            Stock stock = lockOrCreate(tx.getBookId());
            int back = -tx.getQuantityChange();
            applyChange(stock, back);
            record(stock, ORDER_RESTORE, back, orderId, "Hoàn kho khi hủy/trả đơn");
        }
    }

    // ---------- helpers ----------

    private Stock lockOrCreate(Long bookId) {
        return stockRepository.findByIdForUpdate(bookId).orElseGet(() -> {
            Stock s = new Stock();
            s.setBookId(bookId);
            s.setQuantity(0);
            return s;
        });
    }

    private void applyChange(Stock stock, int delta) {
        stock.setQuantity(stock.getQuantity() + delta);
        stock.setUpdatedAt(LocalDateTime.now());
        stockRepository.save(stock);
    }

    private void record(Stock stock, InventoryTxType type, int change, Long orderId, String note) {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setBookId(stock.getBookId());
        tx.setType(type);
        tx.setQuantityChange(change);
        tx.setQuantityAfter(stock.getQuantity());
        tx.setOrderId(orderId);
        tx.setNote(note);
        txRepository.save(tx);
    }

    private StockResponse toResponse(Stock s) {
        return new StockResponse(s.getBookId(), s.getQuantity(), s.getUpdatedAt());
    }

    private TransactionResponse toResponse(InventoryTransaction t) {
        return new TransactionResponse(t.getId(), t.getBookId(), t.getType(), t.getQuantityChange(),
                t.getQuantityAfter(), t.getOrderId(), t.getNote(), t.getCreatedAt());
    }
}