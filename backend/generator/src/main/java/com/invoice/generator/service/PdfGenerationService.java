package com.invoice.generator.service;

import com.invoice.generator.model.Invoice;
import com.invoice.generator.model.InvoiceItem;
import com.invoice.generator.model.Shop;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(40, 40, 40, 40);

        Shop shop = invoice.getShop();

        // --- Header Table ---
        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        header.setWidth(UnitValue.createPercentValue(100));
        header.setMarginBottom(20);

        // Left Cell: Shop Info and Logo (This part is unchanged and working)
        Cell shopInfoCell = new Cell().setPadding(0).setBorder(Border.NO_BORDER);

        if (shop.getLogoPath() != null && !shop.getLogoPath().isEmpty()) {
            try {
                String logoFileName = shop.getLogoPath().substring(shop.getLogoPath().lastIndexOf("/") + 1);
                String fullLogoFilePath = uploadDir + "logos" + File.separator + logoFileName;
                File logoFile = new File(fullLogoFilePath);

                if (logoFile.exists() && logoFile.canRead()) {
                    ImageData data = ImageDataFactory.create(fullLogoFilePath);
                    Image img = new Image(data);
                    img.setWidth(UnitValue.createPercentValue(30));
                    img.setMarginBottom(5);
                    shopInfoCell.add(img);
                } else {
                    shopInfoCell.add(createFallbackLogoText());
                }
            } catch (Exception e) {
                shopInfoCell.add(createFallbackLogoText());
            }
        } else {
            shopInfoCell.add(createFallbackLogoText());
        }
        
        shopInfoCell.add(new Paragraph(shop.getShopName()).setBold().setFontSize(14));
        shopInfoCell.add(new Paragraph(shop.getAddress()).setFontSize(10));
        shopInfoCell.add(new Paragraph("GSTIN: " + shop.getGstin()).setFontSize(10));
        header.addCell(shopInfoCell);

        // Right Cell: Invoice Details
        Cell invoiceDetailsCell = new Cell().setPadding(0).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        invoiceDetailsCell.add(new Paragraph("INVOICE").setBold().setFontSize(24).setMarginBottom(5));
        invoiceDetailsCell.add(new Paragraph("Invoice #: " + invoice.getInvoiceNumber()).setFontSize(10));
        invoiceDetailsCell.add(new Paragraph("Status: " + invoice.getStatus().name()).setFontSize(10));
        invoiceDetailsCell.add(new Paragraph("Date: " + invoice.getIssueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).setFontSize(10).setMarginTop(10));
        header.addCell(invoiceDetailsCell);
        document.add(header);
        
        // --- Billed To Details ---
        document.add(new Paragraph("Billed To:").setBold().setFontSize(10).setMarginBottom(2));
        document.add(new Paragraph(invoice.getCustomer().getName()).setFontSize(10));
        if (invoice.getCustomer().getPhoneNumber() != null && !invoice.getCustomer().getPhoneNumber().isEmpty()) {
            document.add(new Paragraph("Phone: " + invoice.getCustomer().getPhoneNumber()).setFontSize(10));
        }
        if (invoice.getCustomer().getEmail() != null && !invoice.getCustomer().getEmail().isEmpty()) {
            document.add(new Paragraph("Email: " + invoice.getCustomer().getEmail()).setFontSize(10));
        }

        // --- Items Table ---
        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{5, 2, 2, 2})); // Adjusted column widths
        itemsTable.setWidth(UnitValue.createPercentValue(100)).setMarginTop(20);

        itemsTable.addHeaderCell(createStyledCell("Item Description", TextAlignment.LEFT, true, ColorConstants.LIGHT_GRAY));
        itemsTable.addHeaderCell(createStyledCell("Qty", TextAlignment.CENTER, true, ColorConstants.LIGHT_GRAY));
        itemsTable.addHeaderCell(createStyledCell("Price", TextAlignment.RIGHT, true, ColorConstants.LIGHT_GRAY));
        itemsTable.addHeaderCell(createStyledCell("Total", TextAlignment.RIGHT, true, ColorConstants.LIGHT_GRAY));

        for (InvoiceItem item : invoice.getInvoiceItems()) {
            itemsTable.addCell(createStyledCell(item.getProduct().getName(), TextAlignment.LEFT, false, null));
            itemsTable.addCell(createStyledCell(String.valueOf(item.getQuantity()), TextAlignment.CENTER, false, null));
            itemsTable.addCell(createStyledCell("₹" + String.format("%.2f", item.getPricePerUnit()), TextAlignment.RIGHT, false, null));
            itemsTable.addCell(createStyledCell("₹" + String.format("%.2f", item.getTotalAmount()), TextAlignment.RIGHT, false, null));
        }

        // *** THIS IS THE NEW, UPDATED TOTALS SECTION ***
        // It integrates the totals into the main items table for a cleaner look.
        Border topBorder = new SolidBorder(ColorConstants.GRAY, 1f);

        // Subtotal Row
        itemsTable.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER).setBorderTop(topBorder)); // Empty cell spanning first 2 columns
        itemsTable.addCell(createTotalCell("Subtotal:", false).setBorderTop(topBorder));
        itemsTable.addCell(createTotalCell("₹" + String.format("%.2f", invoice.getTotalAmount()), false).setBorderTop(topBorder));
        
        // GST Row
        itemsTable.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER));
        itemsTable.addCell(createTotalCell("GST:", false));
        itemsTable.addCell(createTotalCell("₹" + String.format("%.2f", invoice.getTotalGst()), false));
        
        // Grand Total Row
        itemsTable.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER));
        itemsTable.addCell(createTotalCell("Grand Total:", true));
        itemsTable.addCell(createTotalCell("₹" + String.format("%.2f", invoice.getTotalAmount().add(invoice.getTotalGst())), true));
        
        document.add(itemsTable);
        // The separate totalsTable has been removed.

        document.close();
        return baos.toByteArray();
    }
    
    private Paragraph createFallbackLogoText() {
        return new Paragraph("Shop Logo").setBold().setFontSize(10).setFontColor(ColorConstants.GRAY);
    }

    private Cell createStyledCell(String content, TextAlignment alignment, boolean isBold, com.itextpdf.kernel.colors.Color bgColor) {
        Paragraph p = new Paragraph(content).setFontSize(10).setTextAlignment(alignment);
        if (isBold) p.setBold();
        
        Cell cell = new Cell().add(p).setPadding(5).setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f));
        if (bgColor != null) cell.setBackgroundColor(bgColor);
        
        return cell;
    }

    private Cell createTotalCell(String content, boolean isBold) {
        Paragraph p = new Paragraph(content).setTextAlignment(TextAlignment.RIGHT);
        if (isBold) {
            p.setBold().setFontSize(12);
        } else {
            p.setFontSize(10);
        }
        return new Cell().add(p).setBorder(Border.NO_BORDER).setPadding(2);
    }
}