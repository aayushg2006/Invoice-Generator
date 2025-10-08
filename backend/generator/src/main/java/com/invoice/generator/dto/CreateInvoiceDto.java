package com.invoice.generator.dto;

import com.invoice.generator.model.Invoice;
import lombok.Data;
import java.util.List;

@Data
public class CreateInvoiceDto {
    private Long customerId;
    private String newCustomerName;
    private String newCustomerPhone; // <-- ADDED
    private String newCustomerEmail; // <-- ADDED
    private Invoice.Status status;
    private List<InvoiceItemDto> items;
}