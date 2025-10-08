package com.invoice.generator.controller;

import com.invoice.generator.dto.CreateInvoiceDto;
import com.invoice.generator.dto.InvoiceDetailDto;
import com.invoice.generator.dto.InvoiceSummaryDto;
import com.invoice.generator.model.Invoice;
import com.invoice.generator.service.InvoiceServiceImpl;
import com.invoice.generator.service.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceServiceImpl invoiceService;

    @Autowired
    private PdfGenerationService pdfService;

    @PostMapping
    public ResponseEntity<byte[]> createInvoice(@RequestBody CreateInvoiceDto createInvoiceDto, @AuthenticationPrincipal UserDetails userDetails) {
        // Step 1: Create the invoice in the database as before
        Invoice createdInvoice = invoiceService.createInvoice(createInvoiceDto, userDetails.getUsername());
        
        // Step 2: Generate the PDF for the newly created invoice
        byte[] pdfBytes = pdfService.generateInvoicePdf(createdInvoice);

        // Step 3: Set up HTTP headers to tell the browser to download the file
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // The filename for the download
        headers.setContentDispositionFormData("attachment", "Invoice-" + createdInvoice.getInvoiceNumber() + ".pdf");

        // Step 4: Return the PDF file bytes as the response
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceSummaryDto>> getCurrentUserInvoices(@AuthenticationPrincipal UserDetails userDetails) {
        List<InvoiceSummaryDto> invoices = invoiceService.getInvoicesForUser(userDetails.getUsername());
        return new ResponseEntity<>(invoices, HttpStatus.OK);
    }

    @PutMapping("/{invoiceId}/status")
    public ResponseEntity<String> updateInvoiceStatus(
            @PathVariable Long invoiceId,
            @RequestParam("status") Invoice.Status status,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        invoiceService.updateInvoiceStatus(invoiceId, status, userDetails.getUsername());
        return new ResponseEntity<>("Invoice status updated successfully", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailDto> getInvoiceById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        InvoiceDetailDto invoiceDetails = invoiceService.getInvoiceDetails(id, userDetails.getUsername());
        return ResponseEntity.ok(invoiceDetails);
    }
}