package com.invoice.generator.repository;

import com.invoice.generator.model.Invoice;
import com.invoice.generator.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    // Add this method to find all invoices for a given shop
    List<Invoice> findAllByShop(Shop shop);
}