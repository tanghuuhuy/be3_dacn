package com.bookstore.inventory.entity;

import com.bookstore.inventory.enums.InventoryTxType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Getter @Setter @NoArgsConstructor
public class InventoryTransaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryTxType type;

    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;          // có dấu: +20, -2

    @Column(name = "quantity_after", nullable = false)
    private int quantityAfter;

    @Column(name = "order_id")
    private Long orderId;

    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}