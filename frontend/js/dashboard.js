document.addEventListener('DOMContentLoaded', async () => {
    try {
        // Step 1: Make a single API call to get all dashboard statistics
        const stats = await fetchWithAuth('/dashboard/stats');

        // Step 2: Populate all the stats cards from the response
        document.getElementById('total-revenue').textContent = `₹${stats.totalRevenue.toFixed(2)}`;
        document.getElementById('total-gst-payable').textContent = `₹${stats.totalGstPayable.toFixed(2)}`;
        document.getElementById('invoices-due').textContent = stats.invoicesDue;
        document.getElementById('invoices-paid').textContent = stats.invoicesPaid;

        // Step 3: Fetch recent invoices separately to populate the table
        const recentInvoices = await fetchWithAuth('/invoices');
        const tableBody = document.getElementById('recent-invoices-body');
        tableBody.innerHTML = '';
        
        // Show the 5 most recent invoices
        recentInvoices.slice(0, 5).forEach(invoice => {
            const statusClass = invoice.status === 'PAID' ? 'status-paid' : 'status-pending';
            const row = `
                <tr>
                    <td>${invoice.invoiceNumber}</td>
                    <td>${invoice.customerName}</td>
                    <td>₹${invoice.totalAmount.toFixed(2)}</td>
                    <td><span class="status-badge ${statusClass}">${invoice.status}</span></td>
                </tr>
            `;
            tableBody.innerHTML += row;
        });

    } catch (error) {
        // Errors from fetchWithAuth (like session expired) are handled globally
    }
});