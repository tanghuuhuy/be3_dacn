package com.bookstore.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter @Setter @NoArgsConstructor
public class Stock {

    @Id
    @Column(name = "book_id")
    private Long bookId;                 // id sách bên catalog-service, không tự sinh

    @Column(nullable = false)
    private int quantity;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}