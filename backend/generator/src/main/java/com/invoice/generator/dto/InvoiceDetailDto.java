package com.invoice.generator.dto;

import com.invoice.generator.model.Invoice;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InvoiceDetailDto {
    // Invoice Info
    private String invoiceNumber;
    private LocalDateTime issueDate;
    private Invoice.Status status;
    private BigDecimal subtotal; // Total Amount before GST
    private BigDecimal totalGst;
    private BigDecimal grandTotal;

    // Customer Info
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Shop Info
    private String shopName;
    private String shopAddress;
    private String shopGstin;
    private String shopLogoPath;
    
    // Line Items
    private List<InvoiceItemDetailDto> items;
}