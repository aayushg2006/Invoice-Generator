package com.invoice.generator.service;

import com.invoice.generator.model.Invoice;
import com.invoice.generator.model.InvoiceItem;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationService {

    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        // --- Header ---
        Table header = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        header.setWidth(UnitValue.createPercentValue(100));

        // Shop Details
        Cell shopCell = new Cell().setPadding(10).setBorder(null);
        shopCell.add(new Paragraph(invoice.getShop().getShopName()).setBold().setFontSize(20));
        shopCell.add(new Paragraph(invoice.getShop().getAddress()));
        shopCell.add(new Paragraph("GSTIN: " + invoice.getShop().getGstin()));
        header.addCell(shopCell);

        // Invoice Title
        Cell titleCell = new Cell().setPadding(10).setBorder(null).setTextAlignment(TextAlignment.RIGHT);
        titleCell.add(new Paragraph("INVOICE").setBold().setFontSize(28));
        titleCell.add(new Paragraph("Invoice #: " + invoice.getInvoiceNumber()));
        titleCell.add(new Paragraph("Date: " + invoice.getIssueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        header.addCell(titleCell);
        document.add(header);

        // --- Billed To ---
        document.add(new Paragraph("Billed To:").setBold().setMarginTop(20));
        document.add(new Paragraph(invoice.getCustomer().getName()));

        // --- Items Table ---
        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{4, 1, 2, 2}));
        itemsTable.setWidth(UnitValue.createPercentValue(100)).setMarginTop(20);
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Item Description").setBold()));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold()));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Price").setBold()));
        itemsTable.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()));

        for (InvoiceItem item : invoice.getInvoiceItems()) {
            itemsTable.addCell(new Cell().add(new Paragraph(item.getProduct().getName())));
            itemsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity()))));
            itemsTable.addCell(new Cell().add(new Paragraph("₹" + item.getPricePerUnit().toString())));
            itemsTable.addCell(new Cell().add(new Paragraph("₹" + item.getTotalAmount().toString())));
        }
        document.add(itemsTable);

        // --- Totals ---
        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{75, 25}));
        totalsTable.setWidth(UnitValue.createPercentValue(100)).setMarginTop(20);
        totalsTable.addCell(new Cell().setBorder(null));
        totalsTable.addCell(new Cell().setBorder(null).add(new Paragraph("Subtotal: ₹" + invoice.getTotalAmount())));
        totalsTable.addCell(new Cell().setBorder(null));
        totalsTable.addCell(new Cell().setBorder(null).add(new Paragraph("GST: ₹" + invoice.getTotalGst())));
        totalsTable.addCell(new Cell().setBorder(null));
        totalsTable.addCell(new Cell().setBorder(null).add(new Paragraph("Grand Total: ₹" + invoice.getTotalAmount().add(invoice.getTotalGst())).setBold()));
        document.add(totalsTable);

        document.close();
        return baos.toByteArray();
    }
}