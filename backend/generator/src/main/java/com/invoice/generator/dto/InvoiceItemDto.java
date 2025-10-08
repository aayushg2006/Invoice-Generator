package com.invoice.generator.dto;

import lombok.Data;

@Data
public class InvoiceItemDto {
    private Long productId;
    private int quantity;
}