package com.bookstore.inventory.repository;

import com.bookstore.inventory.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.bookId = :bookId")
    Optional<Stock> findByIdForUpdate(@Param("bookId") Long bookId);

    List<Stock> findByQuantityLessThanEqualOrderByQuantityAsc(int threshold);
}