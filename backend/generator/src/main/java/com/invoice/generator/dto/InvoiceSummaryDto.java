package com.invoice.generator.dto;

import com.invoice.generator.model.Invoice;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InvoiceSummaryDto {
    private Long id;
    private String invoiceNumber;
    private String customerName;
    private BigDecimal totalAmount;
    private Invoice.Status status;
    private LocalDateTime issueDate;
}