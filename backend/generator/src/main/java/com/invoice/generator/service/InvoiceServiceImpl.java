package com.invoice.generator.service;

import com.invoice.generator.dto.CreateInvoiceDto;
import com.invoice.generator.dto.InvoiceDetailDto;
import com.invoice.generator.dto.InvoiceItemDetailDto;
import com.invoice.generator.dto.InvoiceItemDto;
import com.invoice.generator.dto.InvoiceSummaryDto;
import com.invoice.generator.model.*;
import com.invoice.generator.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceServiceImpl {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceItemRepository invoiceItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Invoice createInvoice(CreateInvoiceDto createInvoiceDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Shop shop = user.getShop();

        Customer customer;

        // New Logic: Handle on-the-fly customer creation
        if (createInvoiceDto.getCustomerId() != null) {
            // Use an existing customer
            customer = customerRepository.findById(createInvoiceDto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + createInvoiceDto.getCustomerId()));
        } else if (createInvoiceDto.getNewCustomerName() != null && !createInvoiceDto.getNewCustomerName().isEmpty()) {
            // Create a new customer record
            Customer newCustomer = new Customer();
            newCustomer.setName(createInvoiceDto.getNewCustomerName());
            newCustomer.setShop(shop);
            customer = customerRepository.save(newCustomer);
        } else {
            throw new RuntimeException("Either an existing customer ID or a new customer name must be provided.");
        }
        
        Invoice invoice = new Invoice();
        invoice.setShop(shop);
        invoice.setCustomer(customer);
        invoice.setIssueDate(LocalDateTime.now());
        
        // Use the status from the DTO instead of hardcoding PENDING
        invoice.setStatus(createInvoiceDto.getStatus()); 
        
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setTotalGst(BigDecimal.ZERO);
        
        String invoiceNumber = "INV-" + shop.getId() + "-" + System.currentTimeMillis();
        invoice.setInvoiceNumber(invoiceNumber);
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        BigDecimal totalAmountWithoutGst = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;

        for (InvoiceItemDto itemDto : createInvoiceDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemDto.getProductId()));

            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setProduct(product);
            invoiceItem.setInvoice(savedInvoice);
            invoiceItem.setQuantity(itemDto.getQuantity());
            invoiceItem.setPricePerUnit(product.getSellingPrice());

            BigDecimal itemSubtotal = product.getSellingPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            BigDecimal gstRate = product.getCategory().getGstPercentage().divide(new BigDecimal("100"));
            BigDecimal itemGst = itemSubtotal.multiply(gstRate);
            
            invoiceItem.setGstAmount(itemGst);
            invoiceItem.setTotalAmount(itemSubtotal.add(itemGst));
            
            invoiceItems.add(invoiceItem);

            totalAmountWithoutGst = totalAmountWithoutGst.add(itemSubtotal);
            totalGst = totalGst.add(itemGst);
        }

        invoiceItemRepository.saveAll(invoiceItems);

        savedInvoice.setTotalAmount(totalAmountWithoutGst);
        savedInvoice.setTotalGst(totalGst);
        
        return invoiceRepository.save(savedInvoice);
    }
    
    public List<InvoiceSummaryDto> getInvoicesForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        List<Invoice> invoices = invoiceRepository.findAllByShop(user.getShop());
        
        return invoices.stream()
                .map(this::mapInvoiceToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateInvoiceStatus(Long invoiceId, Invoice.Status status, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));

        // Security check: Ensure the invoice belongs to the current user's shop
        if (!invoice.getShop().getId().equals(user.getShop().getId())) {
            throw new SecurityException("User is not authorized to update this invoice.");
        }

        invoice.setStatus(status);
        invoiceRepository.save(invoice);
    }

    private InvoiceSummaryDto mapInvoiceToSummaryDto(Invoice invoice) {
        InvoiceSummaryDto dto = new InvoiceSummaryDto();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        if (invoice.getCustomer() != null) {
            dto.setCustomerName(invoice.getCustomer().getName());
        } else {
            dto.setCustomerName("N/A");
        }
        dto.setTotalAmount(invoice.getTotalAmount().add(invoice.getTotalGst()));
        dto.setStatus(invoice.getStatus());
        dto.setIssueDate(invoice.getIssueDate());
        return dto;
    }

    // --- NEWLY ADDED METHODS FOR INVOICE DETAILS ---

    public InvoiceDetailDto getInvoiceDetails(Long invoiceId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));

        // Security Check: Ensure the invoice belongs to the user's shop
        if (!invoice.getShop().getId().equals(user.getShop().getId())) {
            throw new SecurityException("User not authorized to view this invoice.");
        }

        return mapInvoiceToDetailDto(invoice);
    }

    private InvoiceDetailDto mapInvoiceToDetailDto(Invoice invoice) {
        InvoiceDetailDto dto = new InvoiceDetailDto();
        
        // Invoice Info
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setIssueDate(invoice.getIssueDate());
        dto.setStatus(invoice.getStatus());
        dto.setSubtotal(invoice.getTotalAmount());
        dto.setTotalGst(invoice.getTotalGst());
        dto.setGrandTotal(invoice.getTotalAmount().add(invoice.getTotalGst()));

        // Customer Info
        if (invoice.getCustomer() != null) {
            dto.setCustomerName(invoice.getCustomer().getName());
            dto.setCustomerPhone(invoice.getCustomer().getPhoneNumber());
            dto.setCustomerEmail(invoice.getCustomer().getEmail());
        }

        // Shop Info
        if (invoice.getShop() != null) {
            dto.setShopName(invoice.getShop().getShopName());
            dto.setShopAddress(invoice.getShop().getAddress());
            dto.setShopGstin(invoice.getShop().getGstin());
            dto.setShopLogoPath(invoice.getShop().getLogoPath());
        }

        // Line Items
        List<InvoiceItemDetailDto> itemDtos = invoiceItemRepository.findAllByInvoice(invoice)
                .stream()
                .map(this::mapInvoiceItemToDetailDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }

    private InvoiceItemDetailDto mapInvoiceItemToDetailDto(InvoiceItem item) {
        InvoiceItemDetailDto dto = new InvoiceItemDetailDto();
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPricePerUnit(item.getPricePerUnit());
        dto.setGstAmount(item.getGstAmount());
        dto.setTotalAmount(item.getTotalAmount());
        return dto;
    }
}