package com.invoice.generator.service;

import com.invoice.generator.dto.DashboardStatsDto;
import com.invoice.generator.model.Invoice;
import com.invoice.generator.model.User;
import com.invoice.generator.repository.InvoiceRepository;
import com.invoice.generator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardServiceImpl {

    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private UserRepository userRepository;

    public DashboardStatsDto getDashboardStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // *** THIS IS THE FIX ***
        // Use the new method name that includes sorting
        List<Invoice> allInvoices = invoiceRepository.findAllByShopOrderByIssueDateDesc(user.getShop());
        
        DashboardStatsDto stats = new DashboardStatsDto();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalGstPayable = BigDecimal.ZERO;
        long invoicesDue = 0;
        long invoicesPaid = 0;

        for (Invoice invoice : allInvoices) {
            if (invoice.getStatus() == Invoice.Status.PAID) {
                invoicesPaid++;
                // Revenue is the amount before GST
                totalRevenue = totalRevenue.add(invoice.getTotalAmount());
                // GST Payable is the GST from paid invoices
                totalGstPayable = totalGstPayable.add(invoice.getTotalGst());
            } else if (invoice.getStatus() == Invoice.Status.PENDING) {
                invoicesDue++;
            }
        }
        
        stats.setTotalRevenue(totalRevenue);
        stats.setInvoicesPaid(invoicesPaid);
        stats.setInvoicesDue(invoicesDue);
        stats.setTotalGstPayable(totalGstPayable);

        return stats;
    }
}