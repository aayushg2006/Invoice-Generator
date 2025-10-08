package com.invoice.generator.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private BigDecimal sellingPrice;
    private Long categoryId;
    private String categoryName; // To display on the frontend
    private BigDecimal gstPercentage; // To display on the frontend
}