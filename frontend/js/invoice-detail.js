document.addEventListener('DOMContentLoaded', async () => {
    const invoiceContent = document.getElementById('invoice-content');
    
    // Get the invoice ID from the URL query parameter
    const urlParams = new URLSearchParams(window.location.search);
    const invoiceId = urlParams.get('id');

    if (!invoiceId) {
        invoiceContent.innerHTML = `<p>Error: No invoice ID provided.</p>`;
        return;
    }

    try {
        const invoice = await fetchWithAuth(`/invoices/${invoiceId}`);
        
        // Helper to format dates
        const formatDate = (dateString) => new Date(dateString).toLocaleDateString();

        let itemsHtml = '';
        invoice.items.forEach(item => {
            itemsHtml += `
                <tr>
                    <td>${item.productName}</td>
                    <td>${item.quantity}</td>
                    <td>₹${item.pricePerUnit.toFixed(2)}</td>
                    <td>₹${item.totalAmount.toFixed(2)}</td>
                </tr>
            `;
        });

        const invoiceHtml = `
            <div class="invoice-header">
                <div>
                    <img src="http://localhost:8080${invoice.shopLogoPath}" alt="Shop Logo" class="shop-logo">
                    <h4>${invoice.shopName}</h4>
                    <p>${invoice.shopAddress}</p>
                    <p>GSTIN: ${invoice.shopGstin}</p>
                </div>
                <div class="invoice-header-right">
                    <h2>INVOICE</h2>
                    <p><strong>Invoice #:</strong> ${invoice.invoiceNumber}</p>
                    <p><strong>Status:</strong> ${invoice.status}</p>
                </div>
            </div>

            <div class="invoice-meta-details">
                <div>
                    <h4>Billed To:</h4>
                    <p>${invoice.customerName}</p>
                    <p>${invoice.customerPhone || ''}</p>
                    <p>${invoice.customerEmail || ''}</p>
                </div>
                <div>
                    <h4>Details:</h4>
                    <p><strong>Issue Date:</strong> ${formatDate(invoice.issueDate)}</p>
                </div>
            </div>

            <table class="product-table invoice-items-detail-table">
                <thead>
                    <tr>
                        <th>Item</th>
                        <th>Quantity</th>
                        <th>Price</th>
                        <th>Line Total</th>
                    </tr>
                </thead>
                <tbody>
                    ${itemsHtml}
                </tbody>
            </table>

            <div class="invoice-totals">
                <div><span>Subtotal:</span> <span>₹${invoice.subtotal.toFixed(2)}</span></div>
                <div><span>Total GST:</span> <span>₹${invoice.totalGst.toFixed(2)}</span></div>
                <div class="grand-total"><span>Grand Total:</span> <span>₹${invoice.grandTotal.toFixed(2)}</span></div>
            </div>
        `;

        invoiceContent.innerHTML = invoiceHtml;

    } catch (error) {
        invoiceContent.innerHTML = `<p>Error loading invoice details: ${error.message}</p>`;
    }
});