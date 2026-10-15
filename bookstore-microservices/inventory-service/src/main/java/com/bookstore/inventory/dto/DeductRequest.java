package com.bookstore.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DeductRequest(
        @NotEmpty @Valid List<StockItemRequest> items
) {}