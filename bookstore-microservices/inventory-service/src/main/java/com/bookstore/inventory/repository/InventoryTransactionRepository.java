package com.bookstore.inventory.repository;

import com.bookstore.inventory.entity.InventoryTransaction;
import com.bookstore.inventory.enums.InventoryTxType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByBookIdOrderByCreatedAtDesc(Long bookId);
    List<InventoryTransaction> findTop100ByOrderByCreatedAtDesc();
    List<InventoryTransaction> findByOrderIdAndType(Long orderId, InventoryTxType type);
    boolean existsByOrderIdAndType(Long orderId, InventoryTxType type);
}