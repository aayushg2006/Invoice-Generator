package com.invoice.generator.dto;

import com.invoice.generator.model.Invoice; // Import the enum
import lombok.Data;
import java.util.List;

@Data
public class CreateInvoiceDto {
    private Long customerId; // Used if an existing customer is selected
    private String newCustomerName; // Used if a new customer is typed
    private Invoice.Status status; // The status to set for the new invoice
    private List<InvoiceItemDto> items;
}